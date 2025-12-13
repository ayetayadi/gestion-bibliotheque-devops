package com.bibliotheque.gestion_bibliotheque.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.UtilisateurService;

@Controller
public class AuthController {

    private final UtilisateurService utilisateurService;

    public AuthController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // LOGIN PAGE
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // REGISTER PAGE
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "auth/register";
    }

    // REGISTER SUBMIT
    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("utilisateur") Utilisateur utilisateur,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            utilisateurService.registerLecteur(utilisateur);
            return "redirect:/login?registered=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}
