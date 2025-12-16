package com.bibliotheque.gestion_bibliotheque.web.superadmin;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.BibliothequeService;
import com.bibliotheque.gestion_bibliotheque.metier.UtilisateurService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Controller
@RequestMapping("/super-admin/admins")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class SuperAdminAdminController {

    private final UtilisateurService utilisateurService;
    private final BibliothequeService bibliothequeService;

    /* =====================================================
     * LISTE DES ADMINS (PAGINATION)
     * ===================================================== */
@GetMapping("")
public String listAdmins(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long biblioId,
        @RequestParam(required = false) Boolean statut,
        Model model
) {

    Page<Utilisateur> admins = utilisateurService.searchAdmins(
            keyword,
            biblioId,
            statut,
            PageRequest.of(page, 10)
    );

    model.addAttribute("admins", admins.getContent());
    model.addAttribute("page", admins);
    model.addAttribute("bibliotheques", bibliothequeService.listAll());

    model.addAttribute("keyword", keyword);
    model.addAttribute("selectedBiblio", biblioId);
    model.addAttribute("selectedStatut", statut);

    model.addAttribute("baseUrl",
            "/super-admin/admins?keyword=" + (keyword != null ? keyword : "")
            + "&biblioId=" + (biblioId != null ? biblioId : "")
            + "&statut=" + (statut != null ? statut : "")
    );

    return "utilisateur/admin/admins";
}

    /* =====================================================
     * FORMULAIRE AJOUT ADMIN
     * ===================================================== */
    @GetMapping("/add")
    public String addAdminForm(Model model) {

        Utilisateur admin = new Utilisateur();
        admin.setBibliotheque(new Bibliotheque()); // nécessaire pour éviter null

        model.addAttribute("admin", admin);
        model.addAttribute("bibliotheques", bibliothequeService.getAll());

        return "utilisateur/admin/admin-form";
    }

    /* =====================================================
     * AJOUT ADMIN
     * ===================================================== */
    @PostMapping("/add")
    public String createAdmin(
            @ModelAttribute Utilisateur admin,
            RedirectAttributes redirectAttributes
    ) {
        try {
            utilisateurService.creerAdministrateur(admin);
            redirectAttributes.addFlashAttribute(
                    "success", "Administrateur ajouté avec succès"
            );
            return "redirect:/super-admin/admins";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/super-admin/admins/add";
        }
    }

    /* =====================================================
     * FORMULAIRE ÉDITION ADMIN
     * ===================================================== */
    @GetMapping("/edit/{id}")
    public String editAdmin(@PathVariable Long id, Model model) {

        Utilisateur admin = utilisateurService.getById(id);

        if (admin.getBibliotheque() == null) {
            admin.setBibliotheque(new Bibliotheque());
        }

        model.addAttribute("admin", admin);
        model.addAttribute("bibliotheques", bibliothequeService.getAll());

        return "utilisateur/admin/admin-form-edit";
    }

    /* =====================================================
     * MISE À JOUR ADMIN
     * ===================================================== */
    @PostMapping("/edit")
    public String updateAdmin(
            @ModelAttribute Utilisateur admin,
            RedirectAttributes redirectAttributes
    ) {
        try {
            utilisateurService.updateAdmin(admin);
            redirectAttributes.addFlashAttribute(
                    "success", "Administrateur modifié avec succès"
            );
            return "redirect:/super-admin/admins";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/super-admin/admins/edit/" + admin.getId();
        }
    }


    // ================= DELETE (SOFT) =================
   @GetMapping("/delete/{id}")
public String deleteAdmin(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes
) {
    utilisateurService.desactiverAdmin(id);
    redirectAttributes.addFlashAttribute(
            "success", "Administrateur désactivé avec succès"
    );
    return "redirect:/super-admin/admins";
}

@GetMapping("/changer-statut/{id}")
public String changerStatutAdmin(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes
) {
    Utilisateur admin = utilisateurService.getById(id);

    try {
        if (admin.isActif()) {
            utilisateurService.desactiverAdmin(id);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Administrateur désactivé : il ne peut plus se connecter."
            );
        } else {
            utilisateurService.activerAdmin(id);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Administrateur réactivé : il peut se connecter à nouveau."
            );
        }
    } catch (IllegalStateException e) {
        // ✅ Message métier affiché à l'utilisateur
        redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
        );
    }

    return "redirect:/super-admin/admins";
}

}
