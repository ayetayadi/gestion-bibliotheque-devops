package com.bibliotheque.gestion_bibliotheque.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.UtilisateurService;

@Controller
public class RegisterController {

    private final UtilisateurService UtilisateurService;

    public RegisterController(UtilisateurService UtilisateurService) {
        this.UtilisateurService = UtilisateurService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute Utilisateur utilisateur,
                                 Model model) {
        try {
            UtilisateurService.registerLecteur(utilisateur);
            return "redirect:/login?registered=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
