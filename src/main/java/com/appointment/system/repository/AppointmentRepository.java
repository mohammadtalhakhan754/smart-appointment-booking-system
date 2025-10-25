package com.appointment.system.repository;

import com.appointment.system.entity.Appointment;
import com.appointment.system.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>,
        JpaSpecificationExecutor<Appointment> {

    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE " +
            "a.doctor.id = :doctorId AND " +
            "a.appointmentDate = :date AND " +
            "((a.startTime <= :startTime AND a.endTime > :startTime) OR " +
            " (a.startTime < :endTime AND a.endTime >= :endTime) OR " +
            " (a.startTime >= :startTime AND a.endTime <= :endTime)) AND " +
            "a.status NOT IN ('CANCELLED') AND " +
            "(:excludeId IS NULL OR a.id != :excludeId)")
    List<Appointment> findConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);

    List<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT a FROM Appointment a WHERE a.patient.user.username = :username")
    Page<Appointment> findByPatientUserUsername(String username, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.user.username = :username")
    Page<Appointment> findByDoctorUserUsername(String username, Pageable pageable);

    boolean existsByIdAndPatientUserUsername(Long appointmentId, String username);

    boolean existsByIdAndDoctorUserUsername(Long appointmentId, String username);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    Long countByStatus(@Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE " +
            "a.appointmentDate = :date AND " +
            "a.status = :status AND " +
            "(:doctorId IS NULL OR a.doctor.id = :doctorId)")
    List<Appointment> findByDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") AppointmentStatus status,
            @Param("doctorId") Long doctorId);
}
