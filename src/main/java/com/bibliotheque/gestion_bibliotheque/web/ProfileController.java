package com.bibliotheque.gestion_bibliotheque.web;

import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.UtilisateurService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UtilisateurService utilisateurService;

    public ProfileController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // ================= AFFICHER PROFIL =================
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {

        String email = authentication.getName(); // username = email
        Utilisateur user = utilisateurService.getByEmail(email);

        model.addAttribute("user", user);

        return "profile/profile";
    }

    // ================= UPDATE PROFIL (AVEC PHOTO) =================
    @PostMapping("/profile")
    public String updateProfile(
            Utilisateur form,
            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String email = authentication.getName();

            utilisateurService.updateProfile(email, form, photoFile);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Profil mis à jour avec succès"
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Erreur lors de la mise à jour du profil"
            );
        }

        return "redirect:/profile";
    }
}
