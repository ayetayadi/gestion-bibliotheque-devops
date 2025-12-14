package com.bibliotheque.gestion_bibliotheque.metier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bibliotheque.gestion_bibliotheque.dao.RessourceRepository;
import com.bibliotheque.gestion_bibliotheque.dao.StockBibliothequeRepository;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeCategorie;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeRessource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RessourceService {

    private final RessourceRepository ressourceRepo;
    private final StockBibliothequeRepository stockRepo;

    // 📌 LISTE DES RESSOURCES
    public List<Ressource> listAll() {
        return ressourceRepo.findAll();
    }

    // 📌 RÉCUPÉRER UNE RESSOURCE
    public Ressource getById(Long id) {
        return ressourceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée"));
    }

    // 📌 RÉCUPÉRER LE STOCK (1 seule méthode !)
    public StockBibliotheque getStock(Ressource r) {
        return stockRepo.findByRessource(r)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));
    }


    // 📌 AJOUTER UNE RESSOURCE
    public Ressource ajouterRessource(
            String titre,
            String auteur,
            TypeRessource typeRessource,
            TypeCategorie categorie,
            int quantiteTotale,
            MultipartFile couvertureFile,
            Utilisateur bibliothecaire
    ) throws Exception {

        Ressource r = new Ressource();
        r.setTitre(titre);
        r.setAuteur(auteur);
        r.setTypeRessource(typeRessource);
        r.setCategorie(categorie);
        r.setBibliotheque(bibliothecaire.getBibliotheque());

        // 📸 Upload couverture
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

        // 📦 STOCK
        StockBibliotheque stock = new StockBibliotheque();
        stock.setBibliotheque(bibliothecaire.getBibliotheque());
        stock.setRessource(saved);
        stock.setQuantiteTotale(quantiteTotale);
        stock.setQuantiteDisponible(quantiteTotale);

        stockRepo.save(stock);

        return saved;
    }


    // 📌 MODIFIER UNE RESSOURCE
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

        // 📸 Upload si nouvelle image
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

        // 🔥 Mise à jour du stock
        StockBibliotheque stock = getStock(r);

        int ancienneTotale = stock.getQuantiteTotale();
        int emprunte = stock.getQuantiteEmpruntee();
        int reserve = stock.getQuantiteReservee();

        stock.setQuantiteTotale(quantiteTotale);

        int nouvelleDispo = quantiteTotale - emprunte - reserve;
        stock.setQuantiteDisponible(Math.max(nouvelleDispo, 0));

        stockRepo.save(stock);

        return ressourceRepo.save(r);
    }
    
    public void supprimerRessource(Long id, Utilisateur bibliothecaire) {

        Ressource r = getById(id);

        // Vérification : le bibliothécaire supprime uniquement dans sa propre bibliothèque
        if (!r.getBibliotheque().getId().equals(bibliothecaire.getBibliotheque().getId())) {
            throw new RuntimeException("Vous ne pouvez supprimer que les ressources de votre bibliothèque.");
        }

        // Supprimer le stock associé
        StockBibliotheque stock = getStock(r);
        stockRepo.delete(stock);

        // Supprimer la ressource
        ressourceRepo.delete(r);
    }

}
