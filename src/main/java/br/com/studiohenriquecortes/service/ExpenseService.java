package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.ExpenseRequest;
import br.com.studiohenriquecortes.dto.ExpenseResponse;
import br.com.studiohenriquecortes.entity.Expense;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .category(request.getCategory())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .notes(request.getNotes())
                .build();

        expenseRepository.save(expense);

        return mapToResponse(expense);
    }

    public List<ExpenseResponse> findAll() {
        return expenseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ExpenseResponse findById(Long id) {
        Expense expense = findExpenseById(id);
        return mapToResponse(expense);
    }

    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense expense = findExpenseById(id);

        expense.setDescription(request.getDescription());
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setNotes(request.getNotes());

        expenseRepository.save(expense);

        return mapToResponse(expense);
    }

    public void delete(Long id) {
        Expense expense = findExpenseById(id);
        expenseRepository.delete(expense);
    }

    private Expense findExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada"));
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .category(expense.getCategory())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .notes(expense.getNotes())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
