package com.appointment.system.controller;

import com.appointment.system.dto.response.ApiResponse;
import com.appointment.system.entity.Service;
import com.appointment.system.exception.ResourceNotFoundException;
import com.appointment.system.repository.ServiceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Tag(name = "Services", description = "Medical services endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ServiceController {

    private final ServiceRepository serviceRepository;

    @GetMapping
    @Operation(summary = "Get all services", description = "Retrieve paginated list of medical services")
    public ResponseEntity<Page<Service>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") boolean activeOnly) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Service> services = activeOnly ?
                serviceRepository.findByIsActiveTrue(pageable) :
                serviceRepository.findAll(pageable);

        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service by ID", description = "Retrieve service details")
    public ResponseEntity<ApiResponse<Service>> getServiceById(@PathVariable Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        return ResponseEntity.ok(ApiResponse.success("Service retrieved successfully", service));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieve list of all service categories")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
        List<String> categories = serviceRepository.findAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create service", description = "Create a new medical service (Admin only)")
    public ResponseEntity<ApiResponse<Service>> createService(@RequestBody Service service) {
        service = serviceRepository.save(service);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service created successfully", service));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update service", description = "Update service details (Admin only)")
    public ResponseEntity<ApiResponse<Service>> updateService(
            @PathVariable Long id,
            @RequestBody Service serviceRequest) {

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        service.setServiceName(serviceRequest.getServiceName());
        service.setDescription(serviceRequest.getDescription());
        service.setDurationMinutes(serviceRequest.getDurationMinutes());
        service.setPrice(serviceRequest.getPrice());
        service.setCategory(serviceRequest.getCategory());
        service.setIsActive(serviceRequest.getIsActive());

        service = serviceRepository.save(service);

        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", service));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete service", description = "Delete a service (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        serviceRepository.delete(service);
        return ResponseEntity.ok(ApiResponse.success("Service deleted successfully", null));
    }
}
