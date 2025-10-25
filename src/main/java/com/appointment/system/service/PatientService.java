package com.appointment.system.service;

import com.appointment.system.entity.Patient;
import com.appointment.system.entity.User;
import com.appointment.system.exception.ResourceNotFoundException;
import com.appointment.system.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Patient getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found for user id: " + userId));
    }

    public Patient createPatient(User user) {
        Patient patient = Patient.builder()
                .user(user)
                .build();

        patient = patientRepository.save(patient);
        log.info("Patient profile created for user: {}", user.getUsername());

        return patient;
    }

    public Patient updatePatient(Long id, Patient patientRequest) {
        Patient patient = getPatientById(id);

        if (patientRequest.getDateOfBirth() != null) {
            patient.setDateOfBirth(patientRequest.getDateOfBirth());
        }
        if (patientRequest.getGender() != null) {
            patient.setGender(patientRequest.getGender());
        }
        if (patientRequest.getAddress() != null) {
            patient.setAddress(patientRequest.getAddress());
        }
        if (patientRequest.getEmergencyContact() != null) {
            patient.setEmergencyContact(patientRequest.getEmergencyContact());
        }
        if (patientRequest.getEmergencyContactName() != null) {
            patient.setEmergencyContactName(patientRequest.getEmergencyContactName());
        }
        if (patientRequest.getMedicalHistory() != null) {
            patient.setMedicalHistory(patientRequest.getMedicalHistory());
        }
        if (patientRequest.getAllergies() != null) {
            patient.setAllergies(patientRequest.getAllergies());
        }
        if (patientRequest.getBloodGroup() != null) {
            patient.setBloodGroup(patientRequest.getBloodGroup());
        }

        patient = patientRepository.save(patient);
        log.info("Patient updated successfully: {}", id);

        return patient;
    }
}
