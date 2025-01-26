package com.example.authdemo.controller;

import com.example.authdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    @PostMapping("/account/delete")
    public String deleteAccount(@RequestParam String password,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            userService.deleteAccount(authentication.getName(), password);
            return "redirect:/login?deleted";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("deleteError", e.getMessage());
            return "redirect:/";
        }
    }
}