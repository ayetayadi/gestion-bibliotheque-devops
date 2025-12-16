package com.bibliotheque.gestion_bibliotheque.metier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.bibliotheque.gestion_bibliotheque.dao.RessourceRepository;
import com.bibliotheque.gestion_bibliotheque.dao.StockBibliothequeRepository;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.StatutRessource;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeCategorie;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeRessource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RessourceService {

    private final RessourceRepository ressourceRepo;
    private final StockBibliothequeRepository stockRepo;

    /* =====================================================
     * 1️⃣ LISTE DES RESSOURCES
     * ===================================================== */
    public List<Ressource> listAll() {
        return ressourceRepo.findAll();
    }

    /* =====================================================
     * 🔍 RECHERCHE + FILTRAGE + PAGINATION
     * ===================================================== */


     public Map<Long, StockBibliotheque> getAllStocksAsMap() {
    return stockRepo.findAll().stream()
            .collect(Collectors.toMap(
                    s -> s.getRessource().getId(),
                    s -> s
            ));
}
    public Page<Ressource> searchCatalogue(
            String keyword,
            TypeCategorie categorie,
            TypeRessource typeRessource,
            Long biblioId,
            Pageable pageable) {

        if (keyword != null && keyword.isBlank())
            keyword = null;

        return ressourceRepo.searchCatalogue(keyword, categorie, typeRessource, biblioId, pageable);
    }

    /* =====================================================
     * 2️⃣ RÉCUPÉRER UNE RESSOURCE PAR ID
     * ===================================================== */
    public Ressource getById(Long id) {
        return ressourceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée"));
    }

    /* =====================================================
     * 3️⃣ STOCK D'UNE RESSOURCE
     * ===================================================== */
    public StockBibliotheque getStock(Ressource r) {
        return stockRepo.findByRessource(r)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé pour cette ressource"));
    }

    /* =====================================================
     * 4️⃣ AJOUT D’UNE RESSOURCE PAR UN BIBLIOTHÉCAIRE
     * ===================================================== */
   public Ressource ajouterRessource(
        String titre,
        String auteur,
        String isbn,
        TypeRessource typeRessource,
        TypeCategorie categorie,
        int quantiteTotale,
        MultipartFile couvertureFile,
        Utilisateur bibliothecaire
) throws Exception {

    Ressource r = new Ressource();
    r.setTitre(titre);
    r.setAuteur(auteur);
    r.setIsbn(isbn); // ⭐ AJOUT IMPORTANT
    r.setTypeRessource(typeRessource);
    r.setCategorie(categorie);
    r.setBibliotheque(bibliothecaire.getBibliotheque());
    r.setStatut(StatutRessource.DISPONIBLE);

    // 📸 Gestion couverture
    if (couvertureFile != null && !couvertureFile.isEmpty()) {

        String original = couvertureFile.getOriginalFilename();
        String ext = original.substring(original.lastIndexOf("."));
        String fileName = "cover_" + System.currentTimeMillis() + ext;

        Path dir = Paths.get("uploads/covers");
        Files.createDirectories(dir);

        Files.copy(
                couvertureFile.getInputStream(),
                dir.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        r.setCheminCouverture(fileName);
    }

    Ressource saved = ressourceRepo.save(r);

    // STOCK
    StockBibliotheque stock = new StockBibliotheque();
    stock.setBibliotheque(bibliothecaire.getBibliotheque());
    stock.setRessource(saved);
    stock.setQuantiteTotale(quantiteTotale);
    stock.setQuantiteDisponible(quantiteTotale);

    stockRepo.save(stock);

    return saved;
}

    /* =====================================================
     * 5️⃣ MODIFICATION D’UNE RESSOURCE
     * ===================================================== */
    public Ressource modifierRessource(
            Long id,
            String titre,
            String auteur,
            TypeRessource typeRessource,
            TypeCategorie categorie,
            int quantiteTotale,
            MultipartFile couvertureFile
    ) throws Exception {

        Ressource r = getById(id);

        r.setTitre(titre);
        r.setAuteur(auteur);
        r.setTypeRessource(typeRessource);
        r.setCategorie(categorie);

        if (couvertureFile != null && !couvertureFile.isEmpty()) {

            String ext = couvertureFile.getOriginalFilename()
                    .substring(couvertureFile.getOriginalFilename().lastIndexOf("."));
            String fileName = "cover_" + System.currentTimeMillis() + ext;

            Path dir = Paths.get("uploads/covers");
            Files.createDirectories(dir);

            Files.copy(
                    couvertureFile.getInputStream(),
                    dir.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING
            );

            r.setCheminCouverture(fileName);
        }

        StockBibliotheque stock = getStock(r);

        int emprunte = stock.getQuantiteEmpruntee();
        int reserve = stock.getQuantiteReservee();

        stock.setQuantiteTotale(quantiteTotale);
        int nouvelleDispo = quantiteTotale - emprunte - reserve;
        stock.setQuantiteDisponible(Math.max(nouvelleDispo, 0));

        stockRepo.save(stock);

        return ressourceRepo.save(r);
    }

    /* =====================================================
     * 6️⃣ SUPPRESSION D’UNE RESSOURCE
     * ===================================================== */
    public void supprimerRessource(Long id, Utilisateur bibliothecaire) {

        Ressource r = getById(id);

        if (!r.getBibliotheque().getId()
                .equals(bibliothecaire.getBibliotheque().getId())) {
            throw new RuntimeException("Vous ne pouvez supprimer que les ressources de votre bibliothèque.");
        }

        StockBibliotheque stock = getStock(r);
        stockRepo.delete(stock);

        ressourceRepo.delete(r);
    }

    /* =====================================================
     * 7️⃣ LISTE PAR BIBLIOTHÈQUE
     * ===================================================== */
    public List<Ressource> listByBibliotheque(Bibliotheque bibliotheque) {
        return ressourceRepo.findByBibliotheque(bibliotheque);
    }
}
