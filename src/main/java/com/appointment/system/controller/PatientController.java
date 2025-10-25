package com.appointment.system.controller;

import com.appointment.system.dto.response.ApiResponse;
import com.appointment.system.dto.response.PatientResponse;
import com.appointment.system.entity.Patient;
import com.appointment.system.mapper.PatientMapper;
import com.appointment.system.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Get patient by ID", description = "Retrieve patient details")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success("Patient retrieved successfully",
                patientMapper.toPatientResponse(patient)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get patient by user ID", description = "Retrieve patient by user ID")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientByUserId(@PathVariable Long userId) {
        Patient patient = patientService.getPatientByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Patient retrieved successfully",
                patientMapper.toPatientResponse(patient)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    @Operation(summary = "Update patient profile", description = "Update patient medical information")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable Long id,
            @RequestBody Patient patientRequest) {

        Patient patient = patientService.updatePatient(id, patientRequest);
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully",
                patientMapper.toPatientResponse(patient)));
    }
}
