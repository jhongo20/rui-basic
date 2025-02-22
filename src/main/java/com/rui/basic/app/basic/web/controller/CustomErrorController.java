package com.rui.basic.app.basic.web.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        log.error("Error occurred: Status={}, Exception={}, Message={}", 
                  status, 
                  exception != null ? exception.toString() : "None", 
                  message != null ? message : "No message");

        model.addAttribute("timestamp", new Date());
        model.addAttribute("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        model.addAttribute("message", message != null ? message : "No hay mensaje de error disponible");

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("status", statusCode);
            
            if (statusCode == 404) {
                model.addAttribute("error", "Página no encontrada");
                model.addAttribute("details", "La página que estás buscando no existe o ha sido movida.");
                return "error/404";
            } else if (statusCode == 403) {
                model.addAttribute("error", "Acceso denegado");
                model.addAttribute("details", "No tienes permisos para acceder a este recurso.");
                return "error/403";
            } else if (statusCode == 401) {
                model.addAttribute("error", "No autorizado");
                model.addAttribute("details", "Debes iniciar sesión para acceder a este recurso.");
                return "error/401";
            } else if (statusCode == 400) {
                model.addAttribute("error", "Solicitud incorrecta");
                model.addAttribute("details", "La solicitud enviada no es válida.");
                return "error/400";
            } else if (statusCode == 405) {
                model.addAttribute("error", "Método no permitido");
                model.addAttribute("details", "El método HTTP utilizado no está permitido para este recurso.");
                return "error/405";
            } else if (statusCode == 500) {
                model.addAttribute("error", "Error interno del servidor");
                model.addAttribute("details", "Ha ocurrido un error interno. Por favor, intenta más tarde.");
                return "error/500";
            } else if (statusCode == 503) {
                model.addAttribute("error", "Servicio no disponible");
                model.addAttribute("details", "El servicio está temporalmente no disponible. Por favor, intenta más tarde.");
                return "error/503";
            } else if (statusCode == 504) {
                model.addAttribute("error", "Tiempo de espera agotado");
                model.addAttribute("details", "El servidor tardó demasiado en responder. Por favor, intenta más tarde.");
                return "error/504";
            }
        }

        // Para errores no específicos o excepciones personalizadas
        if (exception != null) {
            if (exception instanceof AccessDeniedException) {
                model.addAttribute("error", "Acceso denegado");
                model.addAttribute("details", "No tienes permisos para realizar esta acción.");
                return "error/403";
            } else if (exception instanceof AuthenticationException) {
                model.addAttribute("error", "Error de autenticación");
                model.addAttribute("details", "Ha ocurrido un error durante la autenticación.");
                return "error/401";
            } else if (exception instanceof DataAccessException) {
                model.addAttribute("error", "Error de base de datos");
                model.addAttribute("details", "Ha ocurrido un error al acceder a la base de datos.");
                return "error/500";
            }
        }

        // Error genérico para casos no manejados
        model.addAttribute("error", "Error inesperado");
        model.addAttribute("details", "Ha ocurrido un error inesperado. Por favor, contacta al administrador.");
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        log.error("Excepción no manejada: ", ex);
        model.addAttribute("timestamp", new Date());
        model.addAttribute("error", "Error inesperado");
        model.addAttribute("details", "Ha ocurrido un error inesperado. Por favor, contacta al administrador.");
        model.addAttribute("message", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationExceptions(MethodArgumentNotValidException ex, Model model) {
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());
        
        model.addAttribute("timestamp", new Date());
        model.addAttribute("error", "Error de validación");
        model.addAttribute("details", "Los datos proporcionados no son válidos");
        model.addAttribute("validationErrors", errors);
        return "error/400";
    }
}
