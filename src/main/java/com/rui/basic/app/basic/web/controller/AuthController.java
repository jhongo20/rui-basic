package com.rui.basic.app.basic.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.exception.BusinessException;
import com.rui.basic.app.basic.exception.UserAlreadyExistsException;
import com.rui.basic.app.basic.service.DocumentTypeService;
import com.rui.basic.app.basic.service.IntermediaryTypeService;
import com.rui.basic.app.basic.service.RegistrationService;
import com.rui.basic.app.basic.service.UserService;
import com.rui.basic.app.basic.web.dto.RegistrationDto;
import com.rui.basic.app.basic.web.dto.UserRegistrationDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final RegistrationService registrationService;
    private final DocumentTypeService documentTypeService;
    private final IntermediaryTypeService intermediaryTypeService;
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registration", new RegistrationDto());
        model.addAttribute("documentTypes", documentTypeService.getAllDocumentTypes());
        model.addAttribute("intermediaryTypes", intermediaryTypeService.getAllIntermediaryTypes());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registration") RegistrationDto registrationDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Verificar errores de validación
        if (bindingResult.hasErrors()) {
            model.addAttribute("documentTypes", documentTypeService.getAllDocumentTypes());
            model.addAttribute("intermediaryTypes", intermediaryTypeService.getAllIntermediaryTypes());
            return "auth/register";
        }

        // Verificar que las contraseñas coincidan
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            model.addAttribute("passwordError", "Las contraseñas no coinciden");
            model.addAttribute("documentTypes", documentTypeService.getAllDocumentTypes());
            model.addAttribute("intermediaryTypes", intermediaryTypeService.getAllIntermediaryTypes());
            return "auth/register";
        }

        // Verificar que los correos coincidan
        if (!registrationDto.getEmail().equals(registrationDto.getConfirmEmail())) {
            model.addAttribute("emailError", "Los correos electrónicos no coinciden");
            model.addAttribute("documentTypes", documentTypeService.getAllDocumentTypes());
            model.addAttribute("intermediaryTypes", intermediaryTypeService.getAllIntermediaryTypes());
            return "auth/register";
        }

        // Verificar la seguridad de la contraseña
        if (!userService.validatePassword(registrationDto.getPassword())) {
            model.addAttribute("passwordSecurityError", "La contraseña no cumple con los requisitos de seguridad");
            model.addAttribute("documentTypes", documentTypeService.getAllDocumentTypes());
            model.addAttribute("intermediaryTypes", intermediaryTypeService.getAllIntermediaryTypes());
            return "auth/register";
        }

        try {
            // Registrar al usuario
            RuiUser user = registrationService.registerUser(registrationDto);
            // Guardar el email en attributes para mostrar en la página de confirmación
            redirectAttributes.addFlashAttribute("email", user.getUsername());
            return "redirect:/auth/registration-success";
        } catch (UserAlreadyExistsException e) {
            model.addAttribute("userExistsError", e.getMessage());
            model.addAttribute("documentTypes", documentTypeService.getAllDocumentTypes());
            model.addAttribute("intermediaryTypes", intermediaryTypeService.getAllIntermediaryTypes());
            return "auth/register";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("documentTypes", documentTypeService.getAllDocumentTypes());
            model.addAttribute("intermediaryTypes", intermediaryTypeService.getAllIntermediaryTypes());
            return "auth/register";
        }
    }

    @GetMapping("/registration-success")
    public String registrationSuccess() {
        return "auth/registration-success";
    }

    @GetMapping("/confirm")
    public String confirmAccount(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        try {
            registrationService.confirmAccount(token);
            redirectAttributes.addFlashAttribute("message", "La cuenta fue activada correctamente. Ya puede iniciar sesión.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            log.error("Error confirming account", e);
            redirectAttributes.addFlashAttribute("error", "Error activando la cuenta: " + e.getMessage());
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            registrationService.sendPasswordRecoveryEmail(email);
            redirectAttributes.addFlashAttribute("message", "Se ha enviado un correo de recuperación de contraseña a su dirección de email.");
            return "redirect:/auth/login";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/auth/reset-password?token=" + token;
        }

        try {
            registrationService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("message", "La contraseña fue cambiada correctamente. Ya puede iniciar sesión.");
            return "redirect:/auth/login";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/reset-password?token=" + token;
        }
    }
}
