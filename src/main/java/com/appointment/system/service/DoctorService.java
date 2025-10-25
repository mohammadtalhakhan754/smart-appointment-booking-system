package com.appointment.system.service;

import com.appointment.system.dto.request.CreateDoctorRequest;
import com.appointment.system.dto.response.DoctorResponse;
import com.appointment.system.entity.Doctor;
import com.appointment.system.entity.User;
import com.appointment.system.exception.ResourceNotFoundException;
import com.appointment.system.mapper.DoctorMapper;
import com.appointment.system.repository.DoctorRepository;
import com.appointment.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorMapper doctorMapper;

    @Cacheable(value = "doctors", key = "#id")
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        return doctorMapper.toDoctorResponse(doctor);
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAllActiveDoctors(pageable)
                .map(doctorMapper::toDoctorResponse);
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponse> getDoctorsBySpecialization(String specialization, Pageable pageable) {
        return doctorRepository.findBySpecializationContainingIgnoreCase(specialization, pageable)
                .map(doctorMapper::toDoctorResponse);
    }

    @Transactional(readOnly = true)
    public List<String> getAllSpecializations() {
        return doctorRepository.findAllSpecializations();
    }

    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("License number already exists");
        }

        Doctor doctor = doctorMapper.toEntity(request, user);
        doctor = doctorRepository.save(doctor);

        log.info("Doctor created successfully with ID: {}", doctor.getId());
        return doctorMapper.toDoctorResponse(doctor);
    }

    @CacheEvict(value = "doctors", key = "#id")
    public DoctorResponse updateDoctor(Long id, CreateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        doctorMapper.updateEntityFromRequest(doctor, request);
        doctor = doctorRepository.save(doctor);

        log.info("Doctor updated successfully: {}", id);
        return doctorMapper.toDoctorResponse(doctor);
    }

    @CacheEvict(value = "doctors", key = "#id")
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        doctorRepository.delete(doctor);
        log.info("Doctor deleted successfully: {}", id);
    }
}
