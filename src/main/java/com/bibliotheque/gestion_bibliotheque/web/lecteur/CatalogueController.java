package com.bibliotheque.gestion_bibliotheque.web.lecteur;


import org.springframework.data.domain.PageRequest;

import java.util.*;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.entities.pret.StatutPret;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeCategorie;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeRessource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.*;
import com.bibliotheque.gestion_bibliotheque.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;


import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/catalogue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LECTEUR')")
public class CatalogueController {

        private final RessourceService ressourceService;
        private final BibliothequeService bibliothequeService;
        private final RecommendationService recommendationService;
        private final PretRepository pretRepo;
        private final UtilisateurService utilisateurService;


        @GetMapping
        public String catalogue(
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) TypeCategorie categorie,
                        @RequestParam(required = false) TypeRessource type,
                        @RequestParam(required = false) Long biblioId,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {

                // CORRECTION MAJEURE : recharger l'utilisateur depuis la DB
                Utilisateur lecteur = utilisateurService.getByEmail(userDetails.getUsername());


                var pageable = PageRequest.of(page, 8);


                var pageRessources = ressourceService.searchCatalogue(
                                keyword, categorie, type, biblioId, pageable);

                // 🔹 RECOMMANDATIONS
                var recommandations = recommendationService.recommander(lecteur);
                model.addAttribute("recommandations", recommandations);

                // 🔹 RESSOURCES DÉJÀ RÉSERVÉES
                Set<Long> reservees = pretRepo.findByLecteur(lecteur).stream()
                                .filter(p -> p.getStatut() == StatutPret.RESERVE ||
                                                p.getStatut() == StatutPret.EMPRUNTE ||
                                                p.getStatut() == StatutPret.EN_COURS)
                                .map(p -> p.getRessource().getId())
                                .collect(Collectors.toSet());

                model.addAttribute("reservees", reservees);
                model.addAttribute("baseUrl", "/catalogue");

                // STOCKS
                model.addAttribute("stocks", ressourceService.getAllStocksAsMap());

                // Données catalogue
                model.addAttribute("page", pageRessources);
                model.addAttribute("ressources", pageRessources.getContent());
                model.addAttribute("categories", TypeCategorie.values());
                model.addAttribute("types", TypeRessource.values());
                model.addAttribute("bibliotheques", bibliothequeService.listAll());

                return "lecteur/catalogue";
        }
}
