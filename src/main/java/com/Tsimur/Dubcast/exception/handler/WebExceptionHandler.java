package com.Tsimur.Dubcast.exception.handler;

import com.Tsimur.Dubcast.dto.request.RegisterRequest;
import com.Tsimur.Dubcast.exception.type.EmailAlreadyUsedException;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.Tsimur.Dubcast.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public String handleEmailAlreadyUsed(EmailAlreadyUsedException ex,
                                         Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        model.addAttribute("emailError", ex.getMessage());
        return "register";
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException ex,
                                 Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex,
                                Model model) {

        model.addAttribute("message", "Unexpected error. Please try again later.");
        return "error/500";
    }
}









