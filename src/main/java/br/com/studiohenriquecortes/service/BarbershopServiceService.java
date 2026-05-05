package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.*;
import br.com.studiohenriquecortes.entity.BarbershopService;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.BarbershopServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BarbershopServiceService {

    private final BarbershopServiceRepository repository;

    public ServiceResponse create(ServiceRequest request) {

        BarbershopService service = BarbershopService.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationInMinutes(request.getDurationInMinutes())
                .active(true)
                .build();

        repository.save(service);

        return mapToResponse(service);
    }

    public List<ServiceResponse> findAllActive() {
        return repository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ServiceResponse findById(Long id) {
        BarbershopService service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        return mapToResponse(service);
    }

    public ServiceResponse update(Long id, ServiceRequest request) {
        BarbershopService service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setDurationInMinutes(request.getDurationInMinutes());

        repository.save(service);

        return mapToResponse(service);
    }

    public void deactivate(Long id) {
        BarbershopService service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        service.setActive(false);
        repository.save(service);
    }

    private ServiceResponse mapToResponse(BarbershopService service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .durationInMinutes(service.getDurationInMinutes())
                .active(service.getActive())
                .build();
    }
}