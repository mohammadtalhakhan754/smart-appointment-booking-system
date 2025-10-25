package com.appointment.system.repository;

import com.appointment.system.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // ✅ FIXED: Eagerly fetch user with EntityGraph
    @EntityGraph(attributePaths = {"user"})
    Optional<Doctor> findById(Long id);

    // ✅ FIXED: Eagerly fetch user for all
    @EntityGraph(attributePaths = {"user"})
    Page<Doctor> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Optional<Doctor> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<Doctor> findBySpecialization(String specialization);

    // ✅ FIXED: Custom query with join fetch
    @Query("SELECT DISTINCT d FROM Doctor d LEFT JOIN FETCH d.user WHERE LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))")
    Page<Doctor> findBySpecializationContainingIgnoreCase(@Param("specialization") String specialization, Pageable pageable);

    @Query("SELECT DISTINCT d FROM Doctor d LEFT JOIN FETCH d.user")
    Page<Doctor> findAllActiveDoctors(Pageable pageable);

    boolean existsByLicenseNumber(String licenseNumber);

    @Query("SELECT DISTINCT d.specialization FROM Doctor d ORDER BY d.specialization")
    List<String> findAllSpecializations();
}
