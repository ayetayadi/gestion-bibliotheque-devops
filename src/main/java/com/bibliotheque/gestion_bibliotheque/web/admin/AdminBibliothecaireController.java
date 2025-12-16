package com.bibliotheque.gestion_bibliotheque.web.admin;

import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.UtilisateurService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/admin/bibliothecaires")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBibliothecaireController {

    private final UtilisateurService utilisateurService;

    public AdminBibliothecaireController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // ======================= LISTE =======================
@GetMapping
public String list(
        Principal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean statut,
        Model model
) {
    Utilisateur admin = utilisateurService.getByEmail(principal.getName());

    Page<Utilisateur> result =
            utilisateurService.searchBibliothecaires(
                    admin.getBibliotheque().getId(),
                    keyword,
                    statut,
                    PageRequest.of(page, 5)
            );

    model.addAttribute("items", result.getContent());
    model.addAttribute("page", result);

    model.addAttribute("bibliotheque", admin.getBibliotheque());

    // valeurs dans les champs
    model.addAttribute("keyword", keyword);
    model.addAttribute("selectedStatut", statut);

    model.addAttribute("baseUrl",
            "/admin/bibliothecaires?keyword=" + (keyword != null ? keyword : "")
                    + "&statut=" + (statut != null ? statut : "")
    );

    return "utilisateur/bibliothecaire/list";
}

    // ======================= FORM AJOUT =======================
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("bibliothecaire", new Utilisateur());
        return "utilisateur/bibliothecaire/form";
    }

    // ======================= CREATE =======================
  @PostMapping("/add")
public String create(
        @ModelAttribute Utilisateur bibliothecaire,
        Principal principal,
        RedirectAttributes redirectAttributes
) {
    Utilisateur admin = utilisateurService.getByEmail(principal.getName());

    try {
        utilisateurService.creerBibliothecaire(bibliothecaire, admin);
        redirectAttributes.addFlashAttribute("success", "Bibliothécaire ajouté avec succès");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/admin/bibliothecaires/add";
    }

    return "redirect:/admin/bibliothecaires";
}

    // ======================= FORM EDIT =======================
    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Long id,
            Principal principal,
            Model model
    ) {
        Utilisateur admin = utilisateurService.getByEmail(principal.getName());
        Utilisateur biblio = utilisateurService.getById(id);

        if (!biblio.getBibliotheque().getId().equals(admin.getBibliotheque().getId())) {
            throw new RuntimeException("Accès interdit");
        }

        model.addAttribute("bibliothecaire", biblio);
        return "utilisateur/bibliothecaire/form-edit";
    }

    // ======================= UPDATE =======================
@PostMapping("/edit")
public String update(
        @ModelAttribute Utilisateur bibliothecaire,
        Principal principal,
        RedirectAttributes redirectAttributes
) {
    Utilisateur admin = utilisateurService.getByEmail(principal.getName());

    try {
        utilisateurService.updateBibliothecaire(bibliothecaire, admin);
        redirectAttributes.addFlashAttribute("success", "Bibliothécaire modifié avec succès");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/admin/bibliothecaires/edit/" + bibliothecaire.getId();
    }

    return "redirect:/admin/bibliothecaires";
}


    @GetMapping("/changer-statut/{id}")
public String changerStatut(
        @PathVariable Long id,
        Principal principal,
        RedirectAttributes redirectAttributes
) {
    Utilisateur admin = utilisateurService.getByEmail(principal.getName());
    Utilisateur biblio = utilisateurService.getById(id);

    if (biblio.isActif()) {
        utilisateurService.desactiverBibliothecaire(id, admin);
        redirectAttributes.addFlashAttribute(
                "success",
                "Bibliothécaire désactivé"
        );
    } else {
        utilisateurService.activerBibliothecaire(id, admin);
        redirectAttributes.addFlashAttribute(
                "success",
                "Bibliothécaire réactivé"
        );
    }

    return "redirect:/admin/bibliothecaires";
}

}
