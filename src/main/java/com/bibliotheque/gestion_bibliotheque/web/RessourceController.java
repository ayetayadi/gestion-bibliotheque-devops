package com.bibliotheque.gestion_bibliotheque.web;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeCategorie;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeRessource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Role;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.BibliothequeService;
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
    private final BibliothequeService bibliothequeService;

    /* =====================================================
     * 📚 LISTE + FILTRES + PAGINATION
     * ===================================================== */
    @GetMapping({"", "/"})
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TypeCategorie categorie,
            @RequestParam(required = false) TypeRessource typeRessource,
            @RequestParam(required = false) Long biblioId,
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String updated,
            @RequestParam(required = false) String deleted,
            Model model
    ) {

        Utilisateur user = utilisateurService.getCurrentUser();

        // 🔐 Bibliothécaire → SA bibliothèque uniquement
        if (user.getRole() == Role.BIBLIOTHECAIRE) {
            biblioId = user.getBibliotheque().getId();
        }

        // 👑 Admin → toutes les bibliothèques
        if (user.getRole() == Role.ADMIN) {
            model.addAttribute("bibliotheques", bibliothequeService.getAll());
        }

        Page<Ressource> ressourcesPage = ressourceService.searchCatalogue(
                keyword,
                categorie,
                typeRessource,
                biblioId,
                PageRequest.of(page, 8)
        );

        model.addAttribute("ressources", ressourcesPage.getContent());

        // ✅ STOCK SAFE (aucune exception)
        Map<Long, StockBibliotheque> stocks = ressourcesPage.getContent()
                .stream()
                .collect(Collectors.toMap(
                        Ressource::getId,
                        r -> ressourceService.getStockSafe(r)
                ));
        model.addAttribute("stocks", stocks);

        // Pagination
        model.addAttribute("page", ressourcesPage);

        // Filtres conservés
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategorie", categorie);
        model.addAttribute("selectedType", typeRessource);
        model.addAttribute("selectedBiblio", biblioId);

        model.addAttribute("categories", TypeCategorie.values());
        model.addAttribute("typesRessource", TypeRessource.values());

        model.addAttribute("baseUrl",
                "/ressources?keyword=" + (keyword != null ? keyword : "") +
                "&categorie=" + (categorie != null ? categorie : "") +
                "&typeRessource=" + (typeRessource != null ? typeRessource : "") +
                "&biblioId=" + (biblioId != null ? biblioId : "")
        );

        // Messages UI
        model.addAttribute("success", success != null);
        model.addAttribute("updated", updated != null);
        model.addAttribute("deleted", deleted != null);

        return "bibliothecaire/ressources";
    }

    /* =====================================================
     * ➕ FORM AJOUT
     * ===================================================== */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("ressource", new Ressource());
        model.addAttribute("typesRessource", TypeRessource.values());
        model.addAttribute("categories", TypeCategorie.values());
        return "bibliothecaire/ressource-form";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam String titre,
            @RequestParam String auteur,
            @RequestParam(required = false) String isbn,
            @RequestParam TypeRessource typeRessource,
            @RequestParam TypeCategorie categorie,
            @RequestParam int quantiteTotale,
            @RequestParam("couvertureFile") MultipartFile couvertureFile
    ) throws Exception {

        Utilisateur user = utilisateurService.getCurrentUser();

        ressourceService.ajouterRessource(
                titre, auteur, isbn,
                typeRessource, categorie,
                quantiteTotale, couvertureFile, user
        );

        return "redirect:/ressources?success";
    }

    /* =====================================================
     * ✏️ FORM MODIFICATION
     * ===================================================== */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {

        Ressource r = ressourceService.getById(id);
        StockBibliotheque stock = ressourceService.getStockSafe(r);

        model.addAttribute("ressource", r);
        model.addAttribute("stock", stock);
        model.addAttribute("typesRessource", TypeRessource.values());
        model.addAttribute("categories", TypeCategorie.values());

        return "bibliothecaire/ressource-edit";
    }

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

        Utilisateur user = utilisateurService.getCurrentUser();

        ressourceService.modifierRessource(
                id,
                titre,
                auteur,
                typeRessource,
                categorie,
                quantiteTotale,
                couvertureFile,
                user
        );

        return "redirect:/ressources?updated";
    }

    /* =====================================================
     * 🗑️ SUPPRESSION
     * ===================================================== */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        Utilisateur user = utilisateurService.getCurrentUser();
        ressourceService.supprimerRessource(id, user);

        return "redirect:/ressources?deleted";
    }
}
