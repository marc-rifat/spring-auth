package com.example.authdemo.service;

import com.example.authdemo.dto.UserRegistrationDto;
import com.example.authdemo.model.User;
import com.example.authdemo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto registrationDto;
    private User user;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setName("Test User");
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setName(registrationDto.getName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword("encodedPassword");
    }

    @Test
    void registerUser_WithNewEmail_ShouldSucceed() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.registerUser(registrationDto);

        assertNotNull(savedUser);
        assertEquals(registrationDto.getName(), savedUser.getName());
        assertEquals(registrationDto.getEmail(), savedUser.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registrationDto);
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void isEmailAlreadyRegistered_WithExistingEmail_ShouldReturnTrue() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        boolean result = userService.isEmailAlreadyRegistered("test@example.com");

        assertTrue(result);
    }

    @Test
    void isEmailAlreadyRegistered_WithNewEmail_ShouldReturnFalse() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        boolean result = userService.isEmailAlreadyRegistered("test@example.com");

        assertFalse(result);
    }

    @Test
    void deleteAccount_WithValidPassword_ShouldDeleteUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        userService.deleteAccount("test@example.com", "password123");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteAccount_WithInvalidPassword_ShouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteAccount("test@example.com", "wrongpassword");
        });

        assertEquals("Invalid password", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteAccount_WithNonexistentUser_ShouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteAccount("nonexistent@example.com", "password123");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }
}