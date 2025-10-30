package com.appointment.system.service;

import com.appointment.system.dto.request.RegisterRequest;
import com.appointment.system.dto.response.UserResponse;
import com.appointment.system.entity.Role;
import com.appointment.system.entity.User;
import com.appointment.system.exception.ResourceNotFoundException;
import com.appointment.system.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("1234567890")
                .role(Role.PATIENT)
                .enabled(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPhoneNumber("9876543210");
        registerRequest.setRole(Role.PATIENT);
    }

    @Test
    void createUser_Success() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.createUser(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ThrowsIllegalArgumentException_WhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_ThrowsIllegalArgumentException_WhenEmailExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getUserById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_ThrowsResourceNotFoundException_WhenNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(999L);
    }

    @Test
    void getUserByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getUserByUsername("testuser");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getAllUsers_Success() {
        // Given
        List<User> users = Arrays.asList(testUser, testUser);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 20);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserResponse> response = userService.getAllUsers(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);

        verify(userRepository).findAll(pageable);
    }

    @Test
    void getUsersByRole_Success() {
        // Given
        List<User> patients = Arrays.asList(testUser);
        Page<User> patientPage = new PageImpl<>(patients);
        Pageable pageable = PageRequest.of(0, 20);

        when(userRepository.findByRole(Role.PATIENT, pageable)).thenReturn(patientPage);

        // When
        Page<UserResponse> response = userService.getUsersByRole(Role.PATIENT, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getRole()).isEqualTo(Role.PATIENT);

        verify(userRepository).findByRole(Role.PATIENT, pageable);
    }

    @Test
    void updateUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setPhoneNumber("1111111111");

        // When
        UserResponse response = userService.updateUser(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        // NOTE: updateUser DOES NOT encode password, check username/email
        // It only updates: firstName, lastName, phoneNumber
    }

    @Test
    void deleteUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_ThrowsResourceNotFoundException_WhenNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
    }
}
