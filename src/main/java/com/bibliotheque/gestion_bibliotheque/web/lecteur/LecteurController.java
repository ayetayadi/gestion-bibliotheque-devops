package com.bibliotheque.gestion_bibliotheque.web.lecteur;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.PretWorkflowService;
import com.bibliotheque.gestion_bibliotheque.metier.RessourceService;
import com.bibliotheque.gestion_bibliotheque.security.UserDetailsImpl;
import com.bibliotheque.gestion_bibliotheque.entities.pret.StatutPret;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/lecteur")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTEUR')")
public class LecteurController {

    private final PretWorkflowService pretWorkflowService;
    private final PretRepository pretRepository;
    private final RessourceService ressourceService;

    // 1️⃣ Réserver une ressource
@PostMapping("/prets/reserver/{id}")
public String reserver(@PathVariable Long id,
                       @AuthenticationPrincipal UserDetailsImpl userDetails,
                       RedirectAttributes redirectAttrs) {

    if (userDetails == null) return "redirect:/login";

    Utilisateur lecteur = userDetails.getUtilisateur();

    try {
        Ressource res = ressourceService.getById(id);
        StockBibliotheque stock = ressourceService.getStock(res);

        pretWorkflowService.reserverRessource(lecteur, res, stock);

        redirectAttrs.addFlashAttribute("success", "Réservation effectuée !");
    } catch (Exception e) {
        redirectAttrs.addFlashAttribute("error", e.getMessage());
    }

    return "redirect:/catalogue";
}

    // 2️⃣ Afficher mes prêts (exclure les réservations annulées)
@GetMapping("/prets")
public String mesPrets(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(defaultValue = "0") int page,
        Model model) {

    if (userDetails == null) return "redirect:/login";

    Utilisateur lecteur = userDetails.getUtilisateur();

    // Page = 6 éléments par page
    var pageable = PageRequest.of(page, 6);

    var pretsPage = pretRepository.findByLecteur(lecteur, pageable);

    // Exclure ANNULE
    var filteredPage = pretsPage.map(p -> p)
            .filter(p -> p.getStatut() != StatutPret.ANNULE);

    model.addAttribute("page", pretsPage);
    model.addAttribute("prets", pretsPage.getContent());
    model.addAttribute("baseUrl", "/lecteur/prets");

    return "lecteur/mes-prets";
}

    // 3️⃣ Annuler une réservation
    @PostMapping("/prets/annuler/{id}")
    public String annulerReservation(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails,
                                     RedirectAttributes redirectAttrs) {

        if (userDetails == null) return "redirect:/login";

        Utilisateur lecteur = userDetails.getUtilisateur();

        try {
            pretWorkflowService.annulerReservation(id, lecteur);
            redirectAttrs.addFlashAttribute("success", "Réservation annulée.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/lecteur/prets";
    }

    // 4️⃣ Retourner une ressource (mettre à jour date de retour)
    @PostMapping("/prets/retourner/{id}")
    public String retournerPret(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetailsImpl userDetails,
                                RedirectAttributes redirectAttrs) {

        if (userDetails == null) return "redirect:/login";

        Utilisateur lecteur = userDetails.getUtilisateur();

        try {
            pretWorkflowService.retournerPret(id, lecteur);
            redirectAttrs.addFlashAttribute("success", "Ressource retournée !");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/lecteur/prets";
    }
}
