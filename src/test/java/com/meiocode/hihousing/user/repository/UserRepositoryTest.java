package com.meiocode.hihousing.user.repository;

import com.meiocode.hihousing.user.entity.User;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {

        User user = new User(
                "test@example.com",
                "testuser",
                "hashedPassword"
        );

        User savedUser = userRepository.saveAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getUsername()).isEqualTo("testuser");

        Optional<User> foundUser =
                userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail())
                .isEqualTo("test@example.com");
    }

    @Test
    void shouldFindUserByEmailIgnoringCase() {

        User user = new User(
                "test@example.com",
                "testuser",
                "hashedPassword"
        );

        userRepository.saveAndFlush(user);

        Optional<User> foundUser =
                userRepository.findByEmailIgnoreCase("TEST@EXAMPLE.COM");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername())
                .isEqualTo("testuser");
    }

    @Test
    void shouldCheckEmailExistenceIgnoringCase() {

        User user = new User(
                "test@example.com",
                "testuser",
                "hashedPassword"
        );

        userRepository.saveAndFlush(user);

        assertThat(
                userRepository.existsByEmailIgnoreCase(
                        "TEST@EXAMPLE.COM"
                )
        ).isTrue();
    }

    @Test
    void shouldRejectDuplicateEmailWithDifferentCase() {

        User firstUser = new User(
                "test@example.com",
                "user1",
                "hashedPassword"
        );

        userRepository.saveAndFlush(firstUser);

        User secondUser = new User(
                "TEST@EXAMPLE.COM",
                "user2",
                "hashedPassword"
        );

        assertThatThrownBy(
                () -> userRepository.saveAndFlush(secondUser)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
