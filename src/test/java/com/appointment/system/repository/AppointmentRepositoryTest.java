package com.appointment.system.repository;

import com.appointment.system.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// ✅ KEY: No @SpringBootTest, only @DataJpaTest
@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Doctor testDoctor;
    private Patient testPatient;
    private Service testService;

    @BeforeEach
    void setUp() {
        // Create doctor
        User doctorUser = User.builder()
                .username("doctor")
                .email("doctor@test.com")
                .password("password")
                .firstName("Doctor")
                .lastName("Test")
                .phoneNumber("1234567890")
                .role(Role.DOCTOR)
                .enabled(true)
                .accountLocked(false)
                .build();
        entityManager.persist(doctorUser);

        testDoctor = Doctor.builder()
                .user(doctorUser)
                .specialization("Cardiology")
                .licenseNumber("LIC123")
                .yearsOfExperience(10)
                .consultationFee(BigDecimal.valueOf(100))
                .build();
        entityManager.persist(testDoctor);

        // Create patient
        User patientUser = User.builder()
                .username("patient")
                .email("patient@test.com")
                .password("password")
                .firstName("Patient")
                .lastName("Test")
                .phoneNumber("1234567890")
                .role(Role.PATIENT)
                .enabled(true)
                .accountLocked(false)
                .build();
        entityManager.persist(patientUser);

        testPatient = Patient.builder()
                .user(patientUser)
                .build();
        entityManager.persist(testPatient);

        // Create service
        testService = Service.builder()
                .serviceName("Consultation")
                .description("General consultation")
                .durationMinutes(30)
                .price(BigDecimal.valueOf(100))
                .build();
        entityManager.persist(testService);

        entityManager.flush();
    }

    @Test
    void testSaveAppointment() {
        Appointment appointment = Appointment.builder()
                .patient(testPatient)
                .doctor(testDoctor)
                .service(testService)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.PENDING)
                .reasonForVisit("Test appointment")
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.PENDING);
    }

    @Test
    void testFindByStatus() {
        Appointment appointment = Appointment.builder()
                .patient(testPatient)
                .doctor(testDoctor)
                .service(testService)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.PENDING)
                .reasonForVisit("Test")
                .build();

        entityManager.persistAndFlush(appointment);

        Page<Appointment> found = appointmentRepository.findByStatus(
                AppointmentStatus.PENDING,
                PageRequest.of(0, 10)
        );

        assertThat(found.getContent()).hasSize(1);
    }

    @Test
    void testFindConflictingAppointments() {
        Appointment appointment = Appointment.builder()
                .patient(testPatient)
                .doctor(testDoctor)
                .service(testService)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.PENDING)
                .reasonForVisit("Test")
                .build();

        entityManager.persistAndFlush(appointment);

        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                testDoctor.getId(),
                appointment.getAppointmentDate(),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                null
        );

        assertThat(conflicts).hasSize(1);
    }
}
