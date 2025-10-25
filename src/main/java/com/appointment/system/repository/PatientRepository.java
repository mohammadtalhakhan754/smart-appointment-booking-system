package com.appointment.system.repository;

import com.appointment.system.entity.Patient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // ✅ FIXED: Eagerly fetch user
    @EntityGraph(attributePaths = {"user"})
    Optional<Patient> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Patient> findByUserId(Long userId);
}
