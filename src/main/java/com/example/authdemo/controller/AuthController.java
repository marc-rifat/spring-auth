package com.example.authdemo.controller;

import com.example.authdemo.dto.UserRegistrationDto;
import com.example.authdemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        // Check if email already exists
        if (userService.isEmailAlreadyRegistered(userDto.getEmail())) {
            result.rejectValue("email", "error.email", "Email already registered");
            return "register";
        }

        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Please login with your credentials.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("registrationError", "Registration failed: " + e.getMessage());
            return "register";
        }
    }
}