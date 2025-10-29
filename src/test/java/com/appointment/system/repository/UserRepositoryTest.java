package com.appointment.system.repository;

import com.appointment.system.entity.Role;
import com.appointment.system.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// ✅ KEY: No @SpringBootTest, no loading main application
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByUsername() {
        User user = User.builder()
                .username("testuser")
                .email("test@test.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("1234567890")
                .role(Role.PATIENT)
                .enabled(true)
                .accountLocked(false)
                .build();

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void testExistsByUsername() {
        User user = User.builder()
                .username("exists")
                .email("exists@test.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("1234567890")
                .role(Role.PATIENT)
                .enabled(true)
                .accountLocked(false)
                .build();

        entityManager.persistAndFlush(user);

        assertThat(userRepository.existsByUsername("exists")).isTrue();
        assertThat(userRepository.existsByUsername("notexists")).isFalse();
    }

    @Test
    void testFindByRole() {
        User patient = User.builder()
                .username("patient1")
                .email("patient@test.com")
                .password("password")
                .firstName("Patient")
                .lastName("One")
                .phoneNumber("1234567890")
                .role(Role.PATIENT)
                .enabled(true)
                .accountLocked(false)
                .build();

        entityManager.persistAndFlush(patient);

        Page<User> patients = userRepository.findByRole(Role.PATIENT, PageRequest.of(0, 10));

        assertThat(patients.getContent()).hasSize(1);
        assertThat(patients.getContent().get(0).getRole()).isEqualTo(Role.PATIENT);
    }

    @Test
    void testFindAllActiveUsers() {
        User active = User.builder()
                .username("active")
                .email("active@test.com")
                .password("password")
                .firstName("Active")
                .lastName("User")
                .phoneNumber("1234567890")
                .role(Role.PATIENT)
                .enabled(true)
                .accountLocked(false)
                .build();

        User inactive = User.builder()
                .username("inactive")
                .email("inactive@test.com")
                .password("password")
                .firstName("Inactive")
                .lastName("User")
                .phoneNumber("1234567890")
                .role(Role.PATIENT)
                .enabled(false)
                .accountLocked(false)
                .build();

        entityManager.persist(active);
        entityManager.persist(inactive);
        entityManager.flush();

        Page<User> activeUsers = userRepository.findAllActiveUsers(PageRequest.of(0, 10));

        assertThat(activeUsers.getContent()).hasSize(1);
        assertThat(activeUsers.getContent().get(0).getEnabled()).isTrue();
    }
}
