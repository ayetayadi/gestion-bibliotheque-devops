package com.bibliotheque.gestion_bibliotheque.metier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bibliotheque.gestion_bibliotheque.dao.LogAuditRepository;
import com.bibliotheque.gestion_bibliotheque.dao.RessourceRepository;
import com.bibliotheque.gestion_bibliotheque.dao.StockBibliothequeRepository;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.log.LogAudit;
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
    private final LogAuditRepository logAuditRepo;

    /* =====================================================
     * 1️⃣ LISTE & RECHERCHE
     * ===================================================== */

    public List<Ressource> listAll() {
        return ressourceRepo.findAll();
    }

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

        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        return ressourceRepo.searchCatalogue(
                keyword, categorie, typeRessource, biblioId, pageable
        );
    }

    /* =====================================================
     * 2️⃣ CONSULTATION
     * ===================================================== */

    public Ressource getById(Long id) {
        return ressourceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée"));
    }

    public StockBibliotheque getStock(Ressource r) {
        return stockRepo.findByRessource(r)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));
    }
    /**
     * 🔒 Récupération SAFE du stock
     * 👉 Utilisée UNIQUEMENT pour l’affichage (liste, catalogue)
     * 👉 Ne lève JAMAIS d’exception
     */
    public StockBibliotheque getStockSafe(Ressource r) {
        return stockRepo.findByRessource(r)
                .orElseGet(() -> {
                    StockBibliotheque s = new StockBibliotheque();
                    s.setRessource(r);
                    s.setBibliotheque(r.getBibliotheque());
                    s.setQuantiteTotale(0);
                    s.setQuantiteDisponible(0);
                    s.setQuantiteEmpruntee(0);
                    s.setQuantiteReservee(0);
                    s.setTotalEmprunts(0);
                    return s;
                });
    }

    /* =====================================================
     * 3️⃣ AJOUT RESSOURCE (AUDITÉ + DÉTAILS)
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
        r.setIsbn(isbn);
        r.setTypeRessource(typeRessource);
        r.setCategorie(categorie);
        r.setBibliotheque(bibliothecaire.getBibliotheque());
        r.setStatut(StatutRessource.DISPONIBLE);

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

        Ressource saved = ressourceRepo.save(r);

        StockBibliotheque stock = new StockBibliotheque();
        stock.setBibliotheque(bibliothecaire.getBibliotheque());
        stock.setRessource(saved);
        stock.setQuantiteTotale(quantiteTotale);
        stock.setQuantiteDisponible(quantiteTotale);
        stock.setQuantiteEmpruntee(0);
        stock.setQuantiteReservee(0);
        stock.setTotalEmprunts(0);

        stockRepo.save(stock);

        // 📝 AUDIT AVEC DÉTAILS
        logAuditRepo.save(
                LogAudit.builder()
                        .dateAction(LocalDateTime.now())
                        .acteur(bibliothecaire)
                        .role(bibliothecaire.getRole())
                        .action("AJOUT_RESSOURCE")
                        .cible(saved.getIsbn() != null ? saved.getIsbn() : saved.getId().toString())
                        .details(
                                "titre=" + saved.getTitre() +
                                "; auteur=" + saved.getAuteur() +
                                "; type=" + saved.getTypeRessource() +
                                "; categorie=" + saved.getCategorie()
                        )
                        .resultat("SUCCESS")
                        .bibliotheque(bibliothecaire.getBibliotheque())
                        .build()
        );

        return saved;
    }

    /* =====================================================
     * 4️⃣ MODIFICATION RESSOURCE (AUDITÉ + DÉTAILS)
     * ===================================================== */

    public Ressource modifierRessource(
            Long id,
            String titre,
            String auteur,
            TypeRessource typeRessource,
            TypeCategorie categorie,
            int quantiteTotale,
            MultipartFile couvertureFile,
            Utilisateur bibliothecaire
    ) throws Exception {

        Ressource r = getById(id);

        String ancienTitre = r.getTitre();
        String ancienAuteur = r.getAuteur();

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
        stock.setQuantiteDisponible(Math.max(quantiteTotale - emprunte - reserve, 0));

        stockRepo.save(stock);
        Ressource saved = ressourceRepo.save(r);

        logAuditRepo.save(
                LogAudit.builder()
                        .dateAction(LocalDateTime.now())
                        .acteur(bibliothecaire)
                        .role(bibliothecaire.getRole())
                        .action("MODIF_RESSOURCE")
                        .cible(saved.getIsbn() != null ? saved.getIsbn() : saved.getId().toString())
                        .details(
                                "ancien[titre=" + ancienTitre + ", auteur=" + ancienAuteur + "] "
                                + "=> nouveau[titre=" + saved.getTitre() + ", auteur=" + saved.getAuteur() + "]"
                        )
                        .resultat("SUCCESS")
                        .bibliotheque(bibliothecaire.getBibliotheque())
                        .build()
        );

        return saved;
    }

    /* =====================================================
     * 5️⃣ SUPPRESSION RESSOURCE (AUDITÉ + DÉTAILS)
     * ===================================================== */

    public void supprimerRessource(Long id, Utilisateur bibliothecaire) {

        Ressource r = getById(id);

        if (!r.getBibliotheque().getId()
                .equals(bibliothecaire.getBibliotheque().getId())) {
            throw new RuntimeException("Suppression interdite");
        }

        StockBibliotheque stock = getStock(r);
        stockRepo.delete(stock);
        ressourceRepo.delete(r);

        logAuditRepo.save(
                LogAudit.builder()
                        .dateAction(LocalDateTime.now())
                        .acteur(bibliothecaire)
                        .role(bibliothecaire.getRole())
                        .action("SUPPRESSION_RESSOURCE")
                        .cible(r.getIsbn() != null ? r.getIsbn() : r.getId().toString())
                        .details("titre=" + r.getTitre() + "; auteur=" + r.getAuteur())
                        .resultat("SUCCESS")
                        .bibliotheque(bibliothecaire.getBibliotheque())
                        .build()
        );
    }

    /* =====================================================
     * 6️⃣ LISTE PAR BIBLIOTHÈQUE
     * ===================================================== */

    public List<Ressource> listByBibliotheque(Bibliotheque bibliotheque) {
        return ressourceRepo.findByBibliotheque(bibliotheque);
    }
}
