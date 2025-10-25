package com.appointment.system.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentRequest {

    private LocalDate appointmentDate;

    private LocalTime startTime;

    private String notes;

    private String reasonForVisit;

    private String prescription;

    private String diagnosis;

    private Boolean followUpRequired;

    private LocalDate followUpDate;
}
