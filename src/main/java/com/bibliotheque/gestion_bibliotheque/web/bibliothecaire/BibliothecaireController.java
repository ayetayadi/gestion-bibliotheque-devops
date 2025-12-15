package com.bibliotheque.gestion_bibliotheque.web.bibliothecaire;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    /* =====================================================
     * 1️⃣ LISTE DES PRÊTS À GÉRER
     * ===================================================== */
    @GetMapping("/prets")
    public String pretsEnCours(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
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

        return "pret/prets-en-cours";
    }

    /* =====================================================
     * 2️⃣ VALIDER UN PRÊT (passer de RESERVE → EMPRUNTE)
     * ===================================================== */
    @PostMapping("/prets/valider/{id}")
    public String validerPret(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) return "redirect:/login";

        Utilisateur bibliothecaire = userDetails.getUtilisateur();

        pretWorkflowService.validerEmprunt(id, bibliothecaire);

        return "redirect:/bibliothecaire/prets";
    }

    /* =====================================================
     * 3️⃣ CLÔTURER UN PRÊT (RETOURNE → CLOTURE)
     * ===================================================== */
    @PostMapping("/prets/cloturer/{id}")
    public String cloturerPret(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "") String commentaire) {

        if (userDetails == null) return "redirect:/login";

        Utilisateur bibliothecaire = userDetails.getUtilisateur();

        pretWorkflowService.cloturerPret(id, bibliothecaire, commentaire);

        return "redirect:/bibliothecaire/prets";
    }
}
