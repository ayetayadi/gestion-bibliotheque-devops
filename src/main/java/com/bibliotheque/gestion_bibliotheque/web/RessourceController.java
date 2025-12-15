package com.bibliotheque.gestion_bibliotheque.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeCategorie;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeRessource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.RessourceService;
import com.bibliotheque.gestion_bibliotheque.metier.UtilisateurService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/ressources")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','BIBLIOTHECAIRE')")
public class RessourceController {

    private final RessourceService ressourceService;
    private final UtilisateurService utilisateurService;

    // 📄 LISTE DES RESSOURCES
    @GetMapping({"", "/"})
    public String list(Model model) {
        List<Ressource> ressources = ressourceService.listAll();

        // Charger les stocks par ressource
        Map<Long, StockBibliotheque> stocks = new HashMap<>();
        for (Ressource r : ressources) {
            stocks.put(r.getId(), ressourceService.getStock(r));
        }

        model.addAttribute("ressources", ressources);
        model.addAttribute("stocks", stocks);

        // Template Thymeleaf : templates/bibliothecaire/ressources.html
        return "bibliothecaire/ressources";
    }

    // ➕ FORMULAIRE AJOUT
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("ressource", new Ressource());
        model.addAttribute("typesRessource", TypeRessource.values());
        model.addAttribute("categories", TypeCategorie.values());
        return "bibliothecaire/ressource-form";
    }

    // 💾 ENREGISTREMENT
    @PostMapping("/save")
    public String save(
            @RequestParam String titre,
            @RequestParam String auteur,
            @RequestParam TypeRessource typeRessource,
            @RequestParam TypeCategorie categorie,
            @RequestParam int quantiteTotale,
            @RequestParam("couvertureFile") MultipartFile couvertureFile
    ) throws Exception {

        Utilisateur utilisateur = utilisateurService.getCurrentUser();

        ressourceService.ajouterRessource(
                titre, auteur, typeRessource, categorie,
                quantiteTotale, couvertureFile, utilisateur
        );

        return "redirect:/ressources/?success";
    }

    // ✏️ FORMULAIRE MODIFICATION
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Ressource r = ressourceService.getById(id);
        StockBibliotheque stock = ressourceService.getStock(r);

        model.addAttribute("ressource", r);
        model.addAttribute("stock", stock);
        model.addAttribute("typesRessource", TypeRessource.values());
        model.addAttribute("categories", TypeCategorie.values());

        return "bibliothecaire/ressource-edit";
    }

    // 💾 MISE À JOUR
    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam String titre,
            @RequestParam String auteur,
            @RequestParam TypeRessource typeRessource,
            @RequestParam TypeCategorie categorie,
            @RequestParam int quantiteTotale,
            @RequestParam("couvertureFile") MultipartFile couvertureFile
    ) throws Exception {

        ressourceService.modifierRessource(
                id, titre, auteur, typeRessource, categorie, quantiteTotale, couvertureFile
        );

        return "redirect:/ressources/?updated";
    }

    // ❌ SUPPRESSION
    @GetMapping("/delete/{id}")
    public String deleteRessource(@PathVariable Long id) {
        Utilisateur utilisateur = utilisateurService.getCurrentUser();
        ressourceService.supprimerRessource(id, utilisateur);
        return "redirect:/ressources/?deleted";
    }

}
