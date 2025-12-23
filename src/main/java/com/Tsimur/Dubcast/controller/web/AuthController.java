package com.Tsimur.Dubcast.controller.web;

import com.Tsimur.Dubcast.dto.request.RegisterRequest;
import com.Tsimur.Dubcast.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @GetMapping("/register")
  public String showRegisterForm(Model model) {
    model.addAttribute("registerRequest", new RegisterRequest());
    return "register";
  }

  @GetMapping("/login")
  public String loginPage() {
    return "login";
  }

  @PostMapping("/register")
  public String processRegister(
      @Valid @ModelAttribute("registerRequest") RegisterRequest request,
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return "register";
    }

    authService.register(request);

    return "redirect:/login?registered";
  }

  @GetMapping("/reel-radio-poc")
  public String reelRadioPoc() {
    return "reelRadioPoc";
  }
}
