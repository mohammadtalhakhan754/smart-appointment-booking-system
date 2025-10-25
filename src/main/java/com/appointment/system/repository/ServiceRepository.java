package com.appointment.system.repository;

import com.appointment.system.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    Page<Service> findByIsActiveTrue(Pageable pageable);

    List<Service> findByCategory(String category);

    Page<Service> findByCategoryAndIsActiveTrue(String category, Boolean isActive, Pageable pageable);

    @Query("SELECT DISTINCT s.category FROM Service s WHERE s.isActive = true ORDER BY s.category")
    List<String> findAllActiveCategories();

    List<Service> findByServiceNameContainingIgnoreCase(String serviceName);
}
