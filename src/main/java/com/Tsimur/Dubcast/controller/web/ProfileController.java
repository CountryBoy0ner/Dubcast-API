package com.Tsimur.Dubcast.controller.web;

import com.Tsimur.Dubcast.model.User;
import com.Tsimur.Dubcast.repository.UserRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class ProfileController {

  private final UserRepository userRepository;

  // ------------------ МОЙ ПРОФИЛЬ ------------------

  @GetMapping("/profile")
  public String myProfile(Model model, Principal principal) {
    if (principal == null) {
      return "redirect:/login";
    }

    String email = principal.getName();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("User not found: " + email));

    fillHeader(model, principal);

    model.addAttribute("email", user.getEmail());
    model.addAttribute("username", user.getUsername());
    model.addAttribute("bio", user.getBio());

    // только владелец может редактировать
    model.addAttribute("canEdit", true);

    return "profile";
  }

  // обновление username
  @PostMapping("/profile/username")
  public String updateUsername(@RequestParam("username") String username, Principal principal) {
    if (principal == null) {
      return "redirect:/login";
    }

    String email = principal.getName();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("User not found: " + email));

    user.setUsername(username);
    userRepository.save(user);

    return "redirect:/profile?usernameUpdated";
  }

  // обновление bio
  @PostMapping("/profile/bio")
  public String updateBio(@RequestParam("bio") String bio, Principal principal) {
    if (principal == null) {
      return "redirect:/login";
    }

    String email = principal.getName();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("User not found: " + email));

    user.setBio(bio);
    userRepository.save(user);

    return "redirect:/profile?bioUpdated";
  }

  // ------------------ ПУБЛИЧНЫЙ ПРОФИЛЬ ПО USERNAME ------------------

  @GetMapping("/profile/{username}")
  public String publicProfile(
      @PathVariable("username") String username, Model model, Principal principal) {

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    fillHeader(model, principal);

    model.addAttribute("email", user.getEmail());
    model.addAttribute("username", user.getUsername());
    model.addAttribute("bio", user.getBio());

    boolean isOwner = principal != null && principal.getName().equals(user.getEmail());
    model.addAttribute("canEdit", isOwner); // формы только если это мой профиль

    return "profile";
  }

  // ------------------ общие данные для шапки ------------------

  private void fillHeader(Model model, Principal principal) {
    boolean authenticated = (principal != null);
    model.addAttribute("authenticated", authenticated);
    if (authenticated) {
      model.addAttribute("principalName", principal.getName());
    }
  }
}
