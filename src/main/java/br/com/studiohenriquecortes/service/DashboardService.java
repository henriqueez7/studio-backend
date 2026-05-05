package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.BarberCommissionSummaryResponse;
import br.com.studiohenriquecortes.dto.CommissionSummaryResponse;
import br.com.studiohenriquecortes.dto.DashboardSummaryResponse;
import br.com.studiohenriquecortes.dto.FinancialSummaryResponse;
import br.com.studiohenriquecortes.dto.InvestmentSummaryResponse;
import br.com.studiohenriquecortes.dto.StockAlertResponse;
import br.com.studiohenriquecortes.entity.Appointment;
import br.com.studiohenriquecortes.entity.CommissionPayment;
import br.com.studiohenriquecortes.entity.Expense;
import br.com.studiohenriquecortes.entity.Investment;
import br.com.studiohenriquecortes.entity.Product;
import br.com.studiohenriquecortes.entity.ProductSale;
import br.com.studiohenriquecortes.enums.AppointmentStatus;
import br.com.studiohenriquecortes.enums.CommissionPaymentStatus;
import br.com.studiohenriquecortes.enums.InvestmentStatus;
import br.com.studiohenriquecortes.enums.Role;
import br.com.studiohenriquecortes.repository.AppointmentRepository;
import br.com.studiohenriquecortes.repository.BarbershopServiceRepository;
import br.com.studiohenriquecortes.repository.CommissionPaymentRepository;
import br.com.studiohenriquecortes.repository.ExpenseRepository;
import br.com.studiohenriquecortes.repository.InvestmentRepository;
import br.com.studiohenriquecortes.repository.ProductRepository;
import br.com.studiohenriquecortes.repository.ProductSaleRepository;
import br.com.studiohenriquecortes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final BarbershopServiceRepository barbershopServiceRepository;
    private final ProductRepository productRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProductSaleRepository productSaleRepository;
    private final ExpenseRepository expenseRepository;
    private final CommissionPaymentRepository commissionPaymentRepository;
    private final InvestmentRepository investmentRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        long totalClients = userRepository.findByRoleAndActiveTrue(Role.CLIENTE).size();
        long totalBarbers = userRepository.findByRoleAndActiveTrue(Role.BARBEIRO).size();
        long totalServices = barbershopServiceRepository.findByActiveTrue().size();
        long totalActiveProducts = productRepository.findByActiveTrue().size();
        long totalAppointments = appointmentRepository.count();

        return DashboardSummaryResponse.builder()
                .totalClients(totalClients)
                .totalBarbers(totalBarbers)
                .totalServices(totalServices)
                .totalActiveProducts(totalActiveProducts)
                .totalAppointments(totalAppointments)
                .build();
    }

    @Transactional(readOnly = true)
    public FinancialSummaryResponse getFinancialSummary() {
        List<Appointment> appointments = appointmentRepository.findAllDetailed();
        List<ProductSale> productSales = productSaleRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();
        List<CommissionPayment> commissionPayments = commissionPaymentRepository.findAll();

        BigDecimal revenueFromServices = appointments.stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.CONCLUIDO)
                .map(Appointment::getTotalPrice)
                .reduce(BigDecimal.ZERO, this::sumNullable);

        BigDecimal revenueFromProductSales = productSales.stream()
                .map(ProductSale::getTotalPrice)
                .reduce(BigDecimal.ZERO, this::sumNullable);

        BigDecimal totalRevenue = revenueFromServices.add(revenueFromProductSales);

        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, this::sumNullable);

        BigDecimal totalPaidCommissions = commissionPayments.stream()
                .filter(payment -> payment.getStatus() == CommissionPaymentStatus.PAGO)
                .map(CommissionPayment::getTotalAmount)
                .reduce(BigDecimal.ZERO, this::sumNullable);

        BigDecimal netProfit = totalRevenue
                .subtract(totalExpenses)
                .subtract(totalPaidCommissions);

        return FinancialSummaryResponse.builder()
                .revenueFromServices(revenueFromServices)
                .revenueFromProductSales(revenueFromProductSales)
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .totalPaidCommissions(totalPaidCommissions)
                .netProfit(netProfit)
                .build();
    }

    @Transactional(readOnly = true)
    public List<StockAlertResponse> getStockAlerts() {
        return productRepository.findByActiveTrue()
                .stream()
                .filter(product -> product.getStockQuantity() <= product.getMinimumStock())
                .sorted(Comparator.comparing(Product::getStockQuantity)
                        .thenComparing(Product::getName))
                .map(this::mapToStockAlertResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CommissionSummaryResponse getCommissionSummary() {
        List<CommissionPayment> commissionPayments = commissionPaymentRepository.findAllByOrderByCreatedAtDesc();

        BigDecimal totalPaidCommissions = commissionPayments.stream()
                .filter(payment -> payment.getStatus() == CommissionPaymentStatus.PAGO)
                .map(CommissionPayment::getTotalAmount)
                .reduce(BigDecimal.ZERO, this::sumNullable);

        BigDecimal totalPendingCommissions = commissionPayments.stream()
                .filter(payment -> payment.getStatus() == CommissionPaymentStatus.PENDENTE)
                .map(CommissionPayment::getTotalAmount)
                .reduce(BigDecimal.ZERO, this::sumNullable);

        Map<Long, List<CommissionPayment>> paymentsByBarber = commissionPayments.stream()
                .collect(Collectors.groupingBy(payment -> payment.getBarber().getId()));

        List<BarberCommissionSummaryResponse> commissionsByBarber = paymentsByBarber.values()
                .stream()
                .map(this::mapToBarberCommissionSummary)
                .sorted(Comparator.comparing(BarberCommissionSummaryResponse::getBarberName))
                .toList();

        return CommissionSummaryResponse.builder()
                .totalPaidCommissions(totalPaidCommissions)
                .totalPendingCommissions(totalPendingCommissions)
                .commissionsByBarber(commissionsByBarber)
                .build();
    }

    @Transactional(readOnly = true)
    public InvestmentSummaryResponse getInvestmentSummary() {
        List<Investment> investments = investmentRepository.findAll();

        long plannedCount = investments.stream()
                .filter(investment -> investment.getStatus() == InvestmentStatus.PLANEJADO)
                .count();

        long inProgressCount = investments.stream()
                .filter(investment -> investment.getStatus() == InvestmentStatus.EM_ANDAMENTO)
                .count();

        long completedCount = investments.stream()
                .filter(investment -> investment.getStatus() == InvestmentStatus.CONCLUIDO)
                .count();

        long canceledCount = investments.stream()
                .filter(investment -> investment.getStatus() == InvestmentStatus.CANCELADO)
                .count();

        BigDecimal totalPlannedValue = investments.stream()
                .filter(investment -> investment.getStatus() == InvestmentStatus.PLANEJADO)
                .map(Investment::getEstimatedValue)
                .reduce(BigDecimal.ZERO, this::sumNullable);

        BigDecimal totalCompletedValue = investments.stream()
                .filter(investment -> investment.getStatus() == InvestmentStatus.CONCLUIDO)
                .map(Investment::getEstimatedValue)
                .reduce(BigDecimal.ZERO, this::sumNullable);

        return InvestmentSummaryResponse.builder()
                .plannedCount(plannedCount)
                .inProgressCount(inProgressCount)
                .completedCount(completedCount)
                .canceledCount(canceledCount)
                .totalPlannedValue(totalPlannedValue)
                .totalCompletedValue(totalCompletedValue)
                .build();
    }

    private StockAlertResponse mapToStockAlertResponse(Product product) {
        return StockAlertResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .minimumStock(product.getMinimumStock())
                .build();
    }

    private BarberCommissionSummaryResponse mapToBarberCommissionSummary(List<CommissionPayment> payments) {
        CommissionPayment firstPayment = payments.get(0);

        BigDecimal totalPaid = payments.stream()
                .filter(payment -> payment.getStatus() == CommissionPaymentStatus.PAGO)
                .map(CommissionPayment::getTotalAmount)
                .reduce(BigDecimal.ZERO, this::sumNullable);

        BigDecimal totalPending = payments.stream()
                .filter(payment -> payment.getStatus() == CommissionPaymentStatus.PENDENTE)
                .map(CommissionPayment::getTotalAmount)
                .reduce(BigDecimal.ZERO, this::sumNullable);

        return BarberCommissionSummaryResponse.builder()
                .barberId(firstPayment.getBarber().getId())
                .barberName(firstPayment.getBarber().getName())
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .build();
    }

    private BigDecimal sumNullable(BigDecimal accumulator, BigDecimal value) {
        return accumulator.add(value != null ? value : BigDecimal.ZERO);
    }
}
