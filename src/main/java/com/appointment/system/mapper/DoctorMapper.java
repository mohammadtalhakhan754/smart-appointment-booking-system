package com.appointment.system.mapper;

import com.appointment.system.dto.request.CreateDoctorRequest;
import com.appointment.system.dto.response.DoctorResponse;
import com.appointment.system.entity.Doctor;
import com.appointment.system.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorResponse toDoctorResponse(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        User user = doctor.getUser();

        return DoctorResponse.builder()
                .id(doctor.getId())
                .userId(user.getId())
                .name("Dr. " + user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .specialization(doctor.getSpecialization())
                .licenseNumber(doctor.getLicenseNumber())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .consultationFee(doctor.getConsultationFee())
                .availableFrom(doctor.getAvailableFrom())
                .availableTo(doctor.getAvailableTo())
                .availableDays(doctor.getAvailableDays())
                .bio(doctor.getBio())
                .build();
    }

    public Doctor toEntity(CreateDoctorRequest request, User user) {
        if (request == null || user == null) {
            return null;
        }

        return Doctor.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .licenseNumber(request.getLicenseNumber())
                .yearsOfExperience(request.getYearsOfExperience())
                .consultationFee(request.getConsultationFee())
                .availableFrom(request.getAvailableFrom())
                .availableTo(request.getAvailableTo())
                .availableDays(request.getAvailableDays())
                .bio(request.getBio())
                .build();
    }

    public void updateEntityFromRequest(Doctor doctor, CreateDoctorRequest request) {
        if (request.getSpecialization() != null) {
            doctor.setSpecialization(request.getSpecialization());
        }
        if (request.getYearsOfExperience() != null) {
            doctor.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getConsultationFee() != null) {
            doctor.setConsultationFee(request.getConsultationFee());
        }
        if (request.getAvailableFrom() != null) {
            doctor.setAvailableFrom(request.getAvailableFrom());
        }
        if (request.getAvailableTo() != null) {
            doctor.setAvailableTo(request.getAvailableTo());
        }
        if (request.getAvailableDays() != null) {
            doctor.setAvailableDays(request.getAvailableDays());
        }
        if (request.getBio() != null) {
            doctor.setBio(request.getBio());
        }
    }
}
