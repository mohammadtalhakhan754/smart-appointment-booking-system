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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient testPatient;
    private Doctor testDoctor;
    private Service testService;
    private Appointment testAppointment;
    private CreateAppointmentRequest createRequest;

    @BeforeEach
    void setUp() {
        User patientUser = User.builder()
                .id(1L)
                .username("patient")
                .email("patient@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.PATIENT)
                .build();

        testPatient = Patient.builder()
                .id(1L)
                .user(patientUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("Male")
                .build();

        User doctorUser = User.builder()
                .id(2L)
                .username("doctor")
                .email("doctor@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .role(Role.DOCTOR)
                .build();

        testDoctor = Doctor.builder()
                .id(1L)
                .user(doctorUser)
                .specialization("Cardiology")
                .licenseNumber("LIC123")
                .availableFrom(LocalTime.of(9, 0))
                .availableTo(LocalTime.of(17, 0))
                .build();

        testService = Service.builder()
                .id(1L)
                .serviceName("Consultation")
                .description("General Consultation")
                .durationMinutes(30)
                .price(BigDecimal.valueOf(100.00))
                .category("General")
                .isActive(true)
                .build();

        testAppointment = Appointment.builder()
                .id(1L)
                .patient(testPatient)
                .doctor(testDoctor)
                .service(testService)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.PENDING)
                .reasonForVisit("Regular checkup")
                .build();

        createRequest = new CreateAppointmentRequest();
        createRequest.setPatientId(1L);
        createRequest.setDoctorId(1L);
        createRequest.setServiceId(1L);
        createRequest.setAppointmentDate(LocalDate.now().plusDays(1));
        createRequest.setStartTime(LocalTime.of(10, 0));
        createRequest.setReasonForVisit("Regular checkup");
    }

    @Test
    void createAppointment_Success() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(appointmentRepository.findConflictingAppointments(
                anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class), isNull()))
                .thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        doNothing().when(emailService).sendAppointmentConfirmation(any(Appointment.class));

        // When
        AppointmentResponse response = appointmentService.createAppointment(createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("PENDING");

        verify(patientRepository).findById(1L);
        verify(doctorRepository).findById(1L);
        verify(serviceRepository).findById(1L);
        verify(appointmentRepository).findConflictingAppointments(
                anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class), isNull());
        verify(appointmentRepository).save(any(Appointment.class));
        verify(emailService).sendAppointmentConfirmation(any(Appointment.class));
    }

    @Test
    void createAppointment_ThrowsAppointmentConflictException_WhenConflictExists() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(appointmentRepository.findConflictingAppointments(
                anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class), isNull()))
                .thenReturn(Arrays.asList(testAppointment));

        // When & Then
        assertThatThrownBy(() -> appointmentService.createAppointment(createRequest))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("not available");

        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(emailService, never()).sendAppointmentConfirmation(any(Appointment.class));
    }

    @Test
    void getAppointmentById_Success() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        // When
        AppointmentResponse response = appointmentService.getAppointmentById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPatientName()).contains("John Doe");

        verify(appointmentRepository).findById(1L);
    }

    @Test
    void getAppointmentById_ThrowsResourceNotFoundException_WhenNotFound() {
        // Given
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> appointmentService.getAppointmentById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment not found");

        verify(appointmentRepository).findById(999L);
    }

    @Test
    void getAllAppointments_Success() {
        // Given
        List<Appointment> appointments = Arrays.asList(testAppointment);
        Page<Appointment> appointmentPage = new PageImpl<>(appointments);
        Pageable pageable = PageRequest.of(0, 20);

        when(appointmentRepository.findAll(pageable)).thenReturn(appointmentPage);

        // When
        Page<AppointmentResponse> response = appointmentService.getAllAppointments(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        verify(appointmentRepository).findAll(pageable);
    }

    @Test
    void updateAppointmentStatus_Success() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        AppointmentResponse response = appointmentService.updateAppointmentStatus(1L, "CONFIRMED");

        // Then
        assertThat(response).isNotNull();
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(any(Appointment.class));
        // NOTE: updateAppointmentStatus DOES NOT call notificationService
        // It only: sets status, saves, and logs
    }

    @Test
    void cancelAppointment_Success() {
        // Given
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        appointmentService.cancelAppointment(1L);

        // Then
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(any(Appointment.class));
        // NOTE: cancelAppointment sets status to CANCELLED and saves
    }

    @Test
    void updateAppointment_WithNewTime_ChecksConflicts() {
        // Given
        UpdateAppointmentRequest updateRequest = new UpdateAppointmentRequest();
        updateRequest.setStartTime(LocalTime.of(14, 0));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.findConflictingAppointments(
                anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class), eq(1L)))
                .thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // When
        AppointmentResponse response = appointmentService.updateAppointment(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(appointmentRepository).findConflictingAppointments(
                anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class), eq(1L));
    }
}
