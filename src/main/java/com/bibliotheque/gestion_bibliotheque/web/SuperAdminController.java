package com.bibliotheque.gestion_bibliotheque.web;

import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.UtilisateurService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final UtilisateurService utilisateurService;

    public SuperAdminController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // ================= LISTE =================
    @GetMapping("/admins")
    public String admins(Model model) {
        model.addAttribute("admins", utilisateurService.getAllAdmins());
        return "super_admin/admins";
    }

    // ================= FORM AJOUT =================
    @GetMapping("/admins/add")
    public String addAdminForm() {
        return "super_admin/admin-form";
    }

    // ================= AJOUT =================
    @PostMapping("/admins/add")
    public String createAdmin(
            Utilisateur admin,
            RedirectAttributes redirectAttributes
    ) {
        try {
            utilisateurService.creerAdministrateur(admin);
            redirectAttributes.addFlashAttribute("success",
                    "Administrateur ajouté avec succès");
            return "redirect:/super-admin/admins";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/super-admin/admins/add";
        }
    }

    // ================= FORM UPDATE =================
    @GetMapping("/admins/edit/{id}")
    public String editAdmin(@PathVariable Long id, Model model) {
        model.addAttribute("admin", utilisateurService.getById(id));
        return "super_admin/admin-form-edit";
    }

    // ================= UPDATE =================
    @PostMapping("/admins/edit")
    public String updateAdmin(
            Utilisateur admin,
            RedirectAttributes redirectAttributes
    ) {
        try {
            utilisateurService.updateAdmin(admin);
            redirectAttributes.addFlashAttribute("success",
                    "Administrateur modifié avec succès");
            return "redirect:/super-admin/admins";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/super-admin/admins/edit/" + admin.getId();
        }
    }

    // ================= DELETE =================
    @GetMapping("/admins/delete/{id}")
    public String deleteAdmin(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        utilisateurService.deleteAdmin(id);
        redirectAttributes.addFlashAttribute("success",
                "Administrateur supprimé avec succès");
        return "redirect:/super-admin/admins";
    }
}
