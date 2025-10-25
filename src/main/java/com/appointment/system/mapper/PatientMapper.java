package com.appointment.system.mapper;

import com.appointment.system.dto.response.PatientResponse;
import com.appointment.system.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientResponse toPatientResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        return PatientResponse.builder()
                .id(patient.getId())
                .userId(patient.getUser().getId())
                .username(patient.getUser().getUsername())
                .email(patient.getUser().getEmail())
                .firstName(patient.getUser().getFirstName())
                .lastName(patient.getUser().getLastName())
                .phoneNumber(patient.getUser().getPhoneNumber())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .address(patient.getAddress())
                .emergencyContact(patient.getEmergencyContact())
                .emergencyContactName(patient.getEmergencyContactName())
                .medicalHistory(patient.getMedicalHistory())
                .allergies(patient.getAllergies())
                .bloodGroup(patient.getBloodGroup())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
