package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.InvestmentRequest;
import br.com.studiohenriquecortes.dto.InvestmentResponse;
import br.com.studiohenriquecortes.dto.InvestmentStatusUpdateRequest;
import br.com.studiohenriquecortes.entity.Investment;
import br.com.studiohenriquecortes.enums.InvestmentStatus;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;

    public InvestmentResponse create(InvestmentRequest request) {
        Investment investment = Investment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .estimatedValue(request.getEstimatedValue())
                .priority(request.getPriority())
                .status(InvestmentStatus.PLANEJADO)
                .expectedDate(request.getExpectedDate())
                .notes(request.getNotes())
                .build();

        investmentRepository.save(investment);

        return mapToResponse(investment);
    }

    public List<InvestmentResponse> findAll() {
        return investmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public InvestmentResponse findById(Long id) {
        Investment investment = findInvestmentById(id);
        return mapToResponse(investment);
    }

    public InvestmentResponse update(Long id, InvestmentRequest request) {
        Investment investment = findInvestmentById(id);

        investment.setTitle(request.getTitle());
        investment.setDescription(request.getDescription());
        investment.setEstimatedValue(request.getEstimatedValue());
        investment.setPriority(request.getPriority());
        investment.setExpectedDate(request.getExpectedDate());
        investment.setNotes(request.getNotes());

        investmentRepository.save(investment);

        return mapToResponse(investment);
    }

    public void delete(Long id) {
        Investment investment = findInvestmentById(id);
        investmentRepository.delete(investment);
    }

    public InvestmentResponse updateStatus(Long id, InvestmentStatusUpdateRequest request) {
        Investment investment = findInvestmentById(id);
        investment.setStatus(request.getStatus());
        investmentRepository.save(investment);
        return mapToResponse(investment);
    }

    private Investment findInvestmentById(Long id) {
        return investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investimento não encontrado"));
    }

    private InvestmentResponse mapToResponse(Investment investment) {
        return InvestmentResponse.builder()
                .id(investment.getId())
                .title(investment.getTitle())
                .description(investment.getDescription())
                .estimatedValue(investment.getEstimatedValue())
                .priority(investment.getPriority())
                .status(investment.getStatus())
                .expectedDate(investment.getExpectedDate())
                .notes(investment.getNotes())
                .createdAt(investment.getCreatedAt())
                .updatedAt(investment.getUpdatedAt())
                .build();
    }
}
