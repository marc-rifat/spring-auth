package com.example.authdemo.controller;

import com.example.authdemo.config.TestSecurityConfig;
import com.example.authdemo.service.CustomUserDetailsService;
import com.example.authdemo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(TestSecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteAccount_WithValidPassword_ShouldRedirectToLoginWithDeletedParam() throws Exception {
        doNothing().when(userService).deleteAccount(anyString(), anyString());

        mockMvc.perform(post("/account/delete")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?deleted"));

        verify(userService).deleteAccount("test@example.com", "password123");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteAccount_WithInvalidPassword_ShouldRedirectToHomeWithError() throws Exception {
        doThrow(new RuntimeException("Invalid password"))
                .when(userService).deleteAccount(anyString(), anyString());

        mockMvc.perform(post("/account/delete")
                .param("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("deleteError"));

        verify(userService).deleteAccount("test@example.com", "wrongpassword");
    }

    @Test
    void deleteAccount_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/account/delete")
                .param("password", "password123"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).deleteAccount(anyString(), anyString());
    }
}