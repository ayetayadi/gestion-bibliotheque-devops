package com.bibliotheque.gestion_bibliotheque.web.lecteur;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.PretWorkflowService;
import com.bibliotheque.gestion_bibliotheque.metier.RessourceService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/lecteur")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTEUR')")
public class LecteurController {

    private final PretWorkflowService pretWorkflowService;
    private final PretRepository pretRepository;
    private final RessourceService ressourceService;

    /* =====================================================
     * 1️⃣ VOIR MES PRÊTS
     * ===================================================== */
    @GetMapping("/prets")
    public String mesPrets(@AuthenticationPrincipal Utilisateur lecteur,
                           Model model) {

        model.addAttribute("prets",
                pretRepository.findByLecteur(lecteur));

        return "pret/mes-prets";
    }

    /* =====================================================
     * 2️⃣ RÉSERVER UNE RESSOURCE (VRAIE LOGIQUE)
     * ===================================================== */
    @PostMapping("/prets/reserver/{ressourceId}")
    public String reserverPret(@PathVariable Long ressourceId,
                               @AuthenticationPrincipal Utilisateur lecteur) {

        // 🔎 1. Charger la ressource
        Ressource ressource = ressourceService.getById(ressourceId);

        // 🔎 2. Charger le stock correspondant
        StockBibliotheque stock = ressourceService.getStock(ressource);

        // 🔐 3. Vérification simple de sécurité (optionnelle mais propre)
        if (!stock.getBibliotheque().getId()
                .equals(lecteur.getBibliotheque().getId())) {
            throw new RuntimeException("Ressource non disponible dans votre bibliothèque");
        }

        // 🔁 4. Lancer le workflow métier
        pretWorkflowService.reserverRessource(
                lecteur,
                ressource,
                stock.getBibliotheque(),
                stock
        );

        return "redirect:/lecteur/prets";
    }

    /* =====================================================
     * 3️⃣ RETOURNER UNE RESSOURCE
     * ===================================================== */
    @PostMapping("/prets/retourner/{id}")
    public String retournerPret(@PathVariable Long id,
                                @AuthenticationPrincipal Utilisateur lecteur) {

        pretWorkflowService.retournerRessource(id, lecteur);
        return "redirect:/lecteur/prets";
    }
}
