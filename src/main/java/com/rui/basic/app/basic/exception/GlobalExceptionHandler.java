package com.rui.basic.app.basic.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e, RedirectAttributes redirectAttributes) {
        log.error("Tamaño de carga excedido: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("error", "El tamaño total de los archivos excede el límite permitido. Por favor, suba archivos más pequeños.");
        return "redirect:/intermediary/my-registries";
    }
}