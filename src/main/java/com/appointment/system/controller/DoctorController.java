package com.appointment.system.controller;

import com.appointment.system.dto.request.CreateDoctorRequest;
import com.appointment.system.dto.response.ApiResponse;
import com.appointment.system.dto.response.DoctorResponse;
import com.appointment.system.entity.Doctor;
import com.appointment.system.entity.User;
import com.appointment.system.exception.ResourceNotFoundException;
import com.appointment.system.repository.DoctorRepository;
import com.appointment.system.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all doctors", description = "Retrieve paginated list of doctors")
    public ResponseEntity<Page<DoctorResponse>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String specialization) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Doctor> doctors = specialization != null ?
                doctorRepository.findBySpecializationContainingIgnoreCase(specialization, pageable) :
                doctorRepository.findAllActiveDoctors(pageable);

        return ResponseEntity.ok(doctors.map(this::mapToDoctorResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieve doctor details")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        return ResponseEntity.ok(ApiResponse.success("Doctor retrieved successfully",
                mapToDoctorResponse(doctor)));
    }

    @GetMapping("/specializations")
    @Operation(summary = "Get all specializations", description = "Retrieve list of all doctor specializations")
    public ResponseEntity<ApiResponse<List<String>>> getAllSpecializations() {
        List<String> specializations = doctorRepository.findAllSpecializations();
        return ResponseEntity.ok(ApiResponse.success("Specializations retrieved successfully", specializations));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create doctor profile", description = "Create a new doctor profile (Admin only)")
    public ResponseEntity<ApiResponse<DoctorResponse>> createDoctor(
            @Valid @RequestBody CreateDoctorRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("License number already exists");
        }

        Doctor doctor = Doctor.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .licenseNumber(request.getLicenseNumber())
                .yearsOfExperience(request.getYearsOfExperience())
                .consultationFee(request.getConsultationFee())
                .availableFrom(request.getAvailableFrom())
                .availableTo(request.getAvailableTo())
                .availableDays(request.getAvailableDays())
                .bio(request.getBio())
                .build();

        doctor = doctorRepository.save(doctor);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Doctor profile created successfully",
                        mapToDoctorResponse(doctor)));
    }

    private DoctorResponse mapToDoctorResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .userId(doctor.getUser().getId())
                .name("Dr. " + doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName())
                .email(doctor.getUser().getEmail())
                .phoneNumber(doctor.getUser().getPhoneNumber())
                .specialization(doctor.getSpecialization())
                .licenseNumber(doctor.getLicenseNumber())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .consultationFee(doctor.getConsultationFee())
                .availableFrom(doctor.getAvailableFrom())
                .availableTo(doctor.getAvailableTo())
                .availableDays(doctor.getAvailableDays())
                .bio(doctor.getBio())
                .build();
    }
}
