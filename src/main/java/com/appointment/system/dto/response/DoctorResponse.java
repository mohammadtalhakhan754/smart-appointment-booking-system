package com.appointment.system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String specialization;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private String availableDays;
    private String bio;
}
