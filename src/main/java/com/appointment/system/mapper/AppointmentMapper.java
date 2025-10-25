package com.appointment.system.mapper;

import com.appointment.system.dto.response.AppointmentResponse;
import com.appointment.system.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponseDto(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(getFullName(
                        appointment.getPatient().getUser().getFirstName(),
                        appointment.getPatient().getUser().getLastName()
                ))
                .patientEmail(appointment.getPatient().getUser().getEmail())
                .doctorId(appointment.getDoctor().getId())
                .doctorName("Dr. " + getFullName(
                        appointment.getDoctor().getUser().getFirstName(),
                        appointment.getDoctor().getUser().getLastName()
                ))
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

    private String getFullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
