package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.CommissionCalculationResponse;
import br.com.studiohenriquecortes.dto.CommissionPaymentRequest;
import br.com.studiohenriquecortes.dto.CommissionPaymentResponse;
import br.com.studiohenriquecortes.entity.Appointment;
import br.com.studiohenriquecortes.entity.CommissionPayment;
import br.com.studiohenriquecortes.entity.User;
import br.com.studiohenriquecortes.enums.AppointmentStatus;
import br.com.studiohenriquecortes.enums.CommissionPaymentStatus;
import br.com.studiohenriquecortes.enums.Role;
import br.com.studiohenriquecortes.exception.BusinessException;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.AppointmentRepository;
import br.com.studiohenriquecortes.repository.CommissionPaymentRepository;
import br.com.studiohenriquecortes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommissionPaymentService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final CommissionPaymentRepository commissionPaymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommissionPaymentResponse createOrUpdate(CommissionPaymentRequest request) {
        validatePeriod(request.getPeriodStart(), request.getPeriodEnd());

        User barber = findBarberById(request.getBarberId());
        CommissionCalculationResponse calculation = calculateInternal(barber.getId(), request.getPeriodStart(), request.getPeriodEnd());

        CommissionPayment commissionPayment = commissionPaymentRepository
                .findByBarberIdAndPeriodStartAndPeriodEnd(
                        barber.getId(),
                        request.getPeriodStart(),
                        request.getPeriodEnd()
                )
                .orElseGet(() -> CommissionPayment.builder()
                        .barber(barber)
                        .periodStart(request.getPeriodStart())
                        .periodEnd(request.getPeriodEnd())
                        .status(CommissionPaymentStatus.PENDENTE)
                        .build());

        if (commissionPayment.getStatus() == CommissionPaymentStatus.PAGO) {
            throw new BusinessException("Nao e permitido alterar uma comissao ja paga.");
        }

        commissionPayment.setBarber(barber);
        commissionPayment.setPeriodStart(request.getPeriodStart());
        commissionPayment.setPeriodEnd(request.getPeriodEnd());
        commissionPayment.setTotalAmount(calculation.getCommissionAmount());
        commissionPayment.setNotes(request.getNotes());
        commissionPayment.setStatus(CommissionPaymentStatus.PENDENTE);
        commissionPayment.setPaymentDate(null);

        commissionPaymentRepository.save(commissionPayment);

        return mapToPaymentResponse(commissionPayment);
    }

    @Transactional
    public CommissionPaymentResponse update(Long id, CommissionPaymentRequest request) {
        validatePeriod(request.getPeriodStart(), request.getPeriodEnd());

        CommissionPayment commissionPayment = commissionPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento de comissao nao encontrado"));
        CommissionPaymentStatus currentStatus = commissionPayment.getStatus();
        LocalDate currentPaymentDate = commissionPayment.getPaymentDate();

        User barber = findBarberById(request.getBarberId());

        commissionPaymentRepository
                .findByBarberIdAndPeriodStartAndPeriodEndAndIdNot(
                        barber.getId(),
                        request.getPeriodStart(),
                        request.getPeriodEnd(),
                        id
                )
                .ifPresent(existing -> {
                    throw new BusinessException("Ja existe uma comissao cadastrada para este profissional no mesmo periodo.");
                });

        CommissionCalculationResponse calculation = calculateInternal(
                barber.getId(),
                request.getPeriodStart(),
                request.getPeriodEnd()
        );

        commissionPayment.setBarber(barber);
        commissionPayment.setPeriodStart(request.getPeriodStart());
        commissionPayment.setPeriodEnd(request.getPeriodEnd());
        commissionPayment.setTotalAmount(calculation.getCommissionAmount());
        commissionPayment.setNotes(request.getNotes());
        commissionPayment.setStatus(currentStatus);
        commissionPayment.setPaymentDate(currentStatus == CommissionPaymentStatus.PAGO ? currentPaymentDate : null);

        commissionPaymentRepository.save(commissionPayment);

        return mapToPaymentResponse(commissionPayment);
    }

    @Transactional(readOnly = true)
    public List<CommissionPaymentResponse> findAll() {
        return commissionPaymentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommissionPaymentResponse> findByBarber(Long barberId, String authenticatedEmail) {
        validateBarberOwnershipIfNecessary(barberId, authenticatedEmail);
        findBarberById(barberId);

        return commissionPaymentRepository.findByBarberIdOrderByCreatedAtDesc(barberId)
                .stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CommissionCalculationResponse calculate(Long barberId, LocalDate start, LocalDate end, String authenticatedEmail) {
        validateBarberOwnershipIfNecessary(barberId, authenticatedEmail);
        return calculateInternal(barberId, start, end);
    }

    private CommissionCalculationResponse calculateInternal(Long barberId, LocalDate start, LocalDate end) {
        validatePeriod(start, end);

        User barber = findBarberById(barberId);
        BigDecimal commissionPercentage = barber.getCommissionPercentage() != null
                ? barber.getCommissionPercentage()
                : BigDecimal.ZERO;

        List<Appointment> completedAppointments = appointmentRepository
                .findDetailedByBarberIdAndAppointmentDateBetweenAndStatus(
                        barber.getId(),
                        start,
                        end,
                        AppointmentStatus.CONCLUIDO
                );

        BigDecimal grossServiceAmount = completedAppointments.stream()
                .map(Appointment::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal commissionAmount = grossServiceAmount
                .multiply(commissionPercentage)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

        return CommissionCalculationResponse.builder()
                .barberId(barber.getId())
                .barberName(barber.getName())
                .commissionPercentage(commissionPercentage)
                .periodStart(start)
                .periodEnd(end)
                .grossServiceAmount(grossServiceAmount)
                .commissionAmount(commissionAmount)
                .completedAppointments(completedAppointments.size())
                .build();
    }

    private void validateBarberOwnershipIfNecessary(Long barberId, String authenticatedEmail) {
        User authenticatedUser = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado nao encontrado"));

        if (authenticatedUser.getRole() == Role.BARBEIRO && !authenticatedUser.getId().equals(barberId)) {
            throw new AccessDeniedException("Voce so pode visualizar as suas proprias comissoes.");
        }
    }

    @Transactional
    public CommissionPaymentResponse pay(Long id) {
        CommissionPayment commissionPayment = commissionPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento de comissao nao encontrado"));

        if (commissionPayment.getStatus() == CommissionPaymentStatus.CANCELADO) {
            throw new BusinessException("Nao e permitido pagar uma comissao cancelada.");
        }

        if (commissionPayment.getStatus() == CommissionPaymentStatus.PAGO) {
            throw new BusinessException("A comissao ja foi paga.");
        }

        commissionPayment.setStatus(CommissionPaymentStatus.PAGO);
        commissionPayment.setPaymentDate(LocalDate.now());

        commissionPaymentRepository.save(commissionPayment);

        return mapToPaymentResponse(commissionPayment);
    }

    @Transactional
    public void delete(Long id) {
        CommissionPayment commissionPayment = commissionPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento de comissao nao encontrado"));

        commissionPaymentRepository.delete(commissionPayment);
    }

    private User findBarberById(Long barberId) {
        User user = userRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro nao encontrado"));

        if (user.getRole() != Role.BARBEIRO) {
            throw new BusinessException("O usuario informado nao e um barbeiro.");
        }

        return user;
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BusinessException("A data inicial nao pode ser maior que a data final.");
        }
    }

    private CommissionPaymentResponse mapToPaymentResponse(CommissionPayment commissionPayment) {
        return CommissionPaymentResponse.builder()
                .id(commissionPayment.getId())
                .barberId(commissionPayment.getBarber().getId())
                .barberName(commissionPayment.getBarber().getName())
                .commissionPercentage(commissionPayment.getBarber().getCommissionPercentage())
                .periodStart(commissionPayment.getPeriodStart())
                .periodEnd(commissionPayment.getPeriodEnd())
                .totalAmount(commissionPayment.getTotalAmount())
                .status(commissionPayment.getStatus())
                .paymentDate(commissionPayment.getPaymentDate())
                .notes(commissionPayment.getNotes())
                .createdAt(commissionPayment.getCreatedAt())
                .updatedAt(commissionPayment.getUpdatedAt())
                .build();
    }
}
