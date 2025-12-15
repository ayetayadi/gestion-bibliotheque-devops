package com.bibliotheque.gestion_bibliotheque.web.lecteur;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.BibliothequeService;
import com.bibliotheque.gestion_bibliotheque.metier.RessourceService;
import com.bibliotheque.gestion_bibliotheque.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

import java.util.*;

@Controller
@RequestMapping("/catalogue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTEUR')")
public class CatalogueController {

    private final RessourceService ressourceService;
    private final BibliothequeService bibliothequeService;

    @GetMapping
    public String catalogue(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Long biblioId,
            Model model) {

        if (userDetails == null) return "redirect:/login";

        Utilisateur lecteur = userDetails.getUtilisateur();

        List<Ressource> ressources;

        if (biblioId != null) {
            Bibliotheque b = bibliothequeService.getById(biblioId);
            ressources = ressourceService.listByBibliotheque(b);
        } else {
            ressources = ressourceService.listAll();
        }

        Map<Long, StockBibliotheque> stocks = new HashMap<>();
        for (Ressource r : ressources) {
            stocks.put(r.getId(), ressourceService.getStock(r));
        }

        model.addAttribute("ressources", ressources);
        model.addAttribute("stocks", stocks);
        model.addAttribute("bibliotheques", bibliothequeService.listAll());
        model.addAttribute("selectedBiblio", biblioId);

        return "lecteur/catalogue";
    }
}
