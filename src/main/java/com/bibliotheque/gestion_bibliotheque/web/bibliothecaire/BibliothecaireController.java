package com.bibliotheque.gestion_bibliotheque.web.bibliothecaire;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.entities.pret.StatutPret;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.PretWorkflowService;
import com.bibliotheque.gestion_bibliotheque.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/bibliothecaire")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BIBLIOTHECAIRE')")
public class BibliothecaireController {

    private final PretRepository pretRepository;
    private final PretWorkflowService pretWorkflowService;

    // 1️⃣ Liste des prêts à gérer
    @GetMapping("/prets")
    public String pretsEnCours(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               Model model) {

        if (userDetails == null) return "redirect:/login";

        Utilisateur bibliothecaire = userDetails.getUtilisateur();

        model.addAttribute("prets",
                pretRepository.findPretsByBibliothequeAndStatuts(
                        bibliothecaire.getBibliotheque(),
                        List.of(
                                StatutPret.RESERVE,
                                StatutPret.EMPRUNTE,
                                StatutPret.EN_COURS,
                                StatutPret.RETOURNE
                        )
                ));

        return "bibliothecaire/prets-en-cours";
    }

    // 2️⃣ Valider un prêt (RESERVE → EMPRUNTE)
    @PostMapping("/prets/valider/{id}")
    public String validerPret(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetailsImpl userDetails,
                              RedirectAttributes redirectAttrs) {

        if (userDetails == null) return "redirect:/login";

        Utilisateur bibliothecaire = userDetails.getUtilisateur();

        try {
            pretWorkflowService.validerEmprunt(id, bibliothecaire);
            redirectAttrs.addFlashAttribute("success", "Prêt validé !");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/bibliothecaire/prets";
    }

    // 3️⃣ Retourner une ressource (EMPRUNTE / EN_COURS → RETOURNE)
    @PostMapping("/prets/retourner/{id}")
    public String retournerPret(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetailsImpl userDetails,
                                RedirectAttributes redirectAttrs) {

        if (userDetails == null) return "redirect:/login";

        Utilisateur bibliothecaire = userDetails.getUtilisateur();

        try {
            pretWorkflowService.retournerPret(id, bibliothecaire);
            redirectAttrs.addFlashAttribute("success", "Ressource retournée !");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/bibliothecaire/prets";
    }

    // 4️⃣ Clôturer un prêt (RETOURNE → CLOTURE)
    @PostMapping("/prets/cloturer/{id}")
    public String cloturerPret(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetailsImpl userDetails,
                               @RequestParam(defaultValue = "") String commentaire,
                               RedirectAttributes redirectAttrs) {

        if (userDetails == null) return "redirect:/login";

        Utilisateur bibliothecaire = userDetails.getUtilisateur();

        try {
            pretWorkflowService.cloturerPret(id, bibliothecaire, commentaire);
            redirectAttrs.addFlashAttribute("success", "Prêt clôturé !");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/bibliothecaire/prets";
    }
}
