package com.example.authdemo.controller;

import com.example.authdemo.config.TestSecurityConfig;
import com.example.authdemo.dto.UserRegistrationDto;
import com.example.authdemo.service.CustomUserDetailsService;
import com.example.authdemo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void loginPage_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void registerPage_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void registerUser_WithValidInput_ShouldRedirectToLogin() throws Exception {
        when(userService.isEmailAlreadyRegistered(anyString())).thenReturn(false);

        mockMvc.perform(post("/register")
                .param("name", "Test User")
                .param("email", "test@example.com")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldReturnRegisterView() throws Exception {
        when(userService.isEmailAlreadyRegistered(anyString())).thenReturn(true);

        mockMvc.perform(post("/register")
                .param("name", "Test User")
                .param("email", "test@example.com")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        verify(userService, never()).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    void registerUser_WithInvalidInput_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(post("/register")
                .param("name", "") // Empty name should fail validation
                .param("email", "invalid-email") // Invalid email should fail validation
                .param("password", "123")) // Short password should fail validation
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        verify(userService, never()).registerUser(any(UserRegistrationDto.class));
    }
}