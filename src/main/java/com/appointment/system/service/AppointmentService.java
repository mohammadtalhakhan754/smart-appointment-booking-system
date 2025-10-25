package com.appointment.system.service;

import com.appointment.system.dto.request.CreateAppointmentRequest;
import com.appointment.system.dto.request.UpdateAppointmentRequest;
import com.appointment.system.dto.response.AppointmentResponse;
import com.appointment.system.entity.*;
import com.appointment.system.exception.AppointmentConflictException;
import com.appointment.system.exception.ResourceNotFoundException;
import com.appointment.system.repository.AppointmentRepository;
import com.appointment.system.repository.DoctorRepository;
import com.appointment.system.repository.PatientRepository;
import com.appointment.system.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;
    private final EmailService emailService;

    @CacheEvict(value = "appointments", allEntries = true)
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        com.appointment.system.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        LocalTime endTime = request.getStartTime().plusMinutes(service.getDurationMinutes());

        checkAppointmentConflicts(request.getDoctorId(), request.getAppointmentDate(),
                request.getStartTime(), endTime, null);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .service(service)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .status(AppointmentStatus.PENDING)
                .reasonForVisit(request.getReasonForVisit())
                .notes(request.getNotes())
                .build();

        appointment = appointmentRepository.save(appointment);

        emailService.sendAppointmentConfirmation(appointment);

        log.info("Appointment created successfully with ID: {}", appointment.getId());

        return mapToAppointmentResponse(appointment);
    }

    @Cacheable(value = "appointments", key = "#id")
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        return mapToAppointmentResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable)
                .map(this::mapToAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAppointmentsByPatient(Long patientId, Pageable pageable) {
        return appointmentRepository.findByPatientId(patientId, pageable)
                .map(this::mapToAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAppointmentsByDoctor(Long doctorId, Pageable pageable) {
        return appointmentRepository.findByDoctorId(doctorId, pageable)
                .map(this::mapToAppointmentResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getCurrentUserAppointments(String username, Pageable pageable) {
        return appointmentRepository.findByPatientUserUsername(username, pageable)
                .map(this::mapToAppointmentResponse);
    }

    @CacheEvict(value = "appointments", key = "#id")
    public AppointmentResponse updateAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(request.getAppointmentDate());
        }

        if (request.getStartTime() != null) {
            appointment.setStartTime(request.getStartTime());
            appointment.setEndTime(request.getStartTime()
                    .plusMinutes(appointment.getService().getDurationMinutes()));

            checkAppointmentConflicts(appointment.getDoctor().getId(),
                    appointment.getAppointmentDate(),
                    appointment.getStartTime(),
                    appointment.getEndTime(),
                    id);
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        if (request.getPrescription() != null) {
            appointment.setPrescription(request.getPrescription());
        }

        if (request.getDiagnosis() != null) {
            appointment.setDiagnosis(request.getDiagnosis());
        }

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment updated successfully: {}", id);

        return mapToAppointmentResponse(appointment);
    }

    @CacheEvict(value = "appointments", key = "#id")
    public AppointmentResponse updateAppointmentStatus(Long id, String statusStr) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        AppointmentStatus newStatus = AppointmentStatus.valueOf(statusStr.toUpperCase());
        appointment.setStatus(newStatus);

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment status updated to {}: {}", newStatus, id);

        return mapToAppointmentResponse(appointment);
    }

    @CacheEvict(value = "appointments", key = "#id")
    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        log.info("Appointment cancelled: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean isAppointmentOwner(Long appointmentId, String username) {
        return appointmentRepository.existsByIdAndPatientUserUsername(appointmentId, username);
    }

    @Transactional(readOnly = true)
    public boolean isAppointmentDoctor(Long appointmentId, String username) {
        return appointmentRepository.existsByIdAndDoctorUserUsername(appointmentId, username);
    }

    private void checkAppointmentConflicts(Long doctorId, java.time.LocalDate appointmentDate,
                                           LocalTime startTime, LocalTime endTime, Long excludeId) {
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                doctorId, appointmentDate, startTime, endTime, excludeId);

        if (!conflicts.isEmpty()) {
            throw new AppointmentConflictException(
                    "Doctor is not available during the requested time slot");
        }
    }

    private AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getUser().getFirstName() + " " +
                        appointment.getPatient().getUser().getLastName())
                .patientEmail(appointment.getPatient().getUser().getEmail())
                .doctorId(appointment.getDoctor().getId())
                .doctorName("Dr. " + appointment.getDoctor().getUser().getFirstName() + " " +
                        appointment.getDoctor().getUser().getLastName())
                .doctorSpecialization(appointment.getDoctor().getSpecialization())
                .serviceId(appointment.getService().getId())
                .serviceName(appointment.getService().getServiceName())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus().name())
                .reasonForVisit(appointment.getReasonForVisit())
                .notes(appointment.getNotes())
                .prescription(appointment.getPrescription())
                .diagnosis(appointment.getDiagnosis())
                .followUpRequired(appointment.getFollowUpRequired())
                .followUpDate(appointment.getFollowUpDate())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
