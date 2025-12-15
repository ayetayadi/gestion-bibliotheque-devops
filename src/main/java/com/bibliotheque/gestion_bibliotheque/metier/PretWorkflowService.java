package com.bibliotheque.gestion_bibliotheque.metier;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.dao.StockBibliothequeRepository;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.pret.Pret;
import com.bibliotheque.gestion_bibliotheque.entities.pret.StatutPret;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PretWorkflowService {

    private final PretRepository pretRepository;
    private final StockBibliothequeRepository stockRepository;

    // 1️⃣ RESERVER UNE RESSOURCE
    public Pret reserverRessource(Utilisateur lecteur,
                                  Ressource ressource,
                                  Bibliotheque bibliotheque,
                                  StockBibliotheque stock) {

        if (stock.getQuantiteDisponible() <= 0) {
            throw new IllegalStateException("Aucun exemplaire disponible pour cette ressource.");
        }

        Pret pret = new Pret();
        pret.setLecteur(lecteur);
        pret.setRessource(ressource);
        pret.setBibliotheque(bibliotheque);
        pret.setStockBibliotheque(stock);
        pret.setDateReservation(LocalDateTime.now());
        pret.setStatut(StatutPret.RESERVE);

        return pretRepository.save(pret);
    }

    // 2️⃣ VALIDER UN EMPRUNT (RESERVE → EMPRUNTE)
    public Pret validerEmprunt(Long pretId, Utilisateur bibliothecaire) {

        Pret pret = getPretOrThrow(pretId);

        if (!pret.getBibliotheque().getId()
                .equals(bibliothecaire.getBibliotheque().getId())) {
            throw new SecurityException("Vous ne pouvez gérer que les prêts de votre bibliothèque.");
        }

        if (pret.getStatut() != StatutPret.RESERVE) {
            throw new IllegalStateException("Le prêt doit être au statut RESERVE.");
        }

        StockBibliotheque stock = pret.getStockBibliotheque();
        if (stock.getQuantiteDisponible() <= 0) {
            throw new IllegalStateException("Stock insuffisant.");
        }

        stock.setQuantiteDisponible(stock.getQuantiteDisponible() - 1);
        stockRepository.save(stock);

        pret.setStatut(StatutPret.EMPRUNTE);
        pret.setDateDebutEmprunt(LocalDateTime.now());
        pret.setDateFinPrevu(LocalDateTime.now().plusDays(14));

        return pretRepository.save(pret);
    }

    // 3️⃣ CLÔTURER LE PRÊT (RETOURNE → CLOTURE)
    public Pret cloturerPret(Long pretId, Utilisateur bibliothecaire, String commentaire) {

        Pret pret = getPretOrThrow(pretId);

        if (!pret.getBibliotheque().getId()
                .equals(bibliothecaire.getBibliotheque().getId())) {
            throw new SecurityException("Vous ne pouvez gérer que les prêts de votre bibliothèque.");
        }

        if (pret.getStatut() != StatutPret.RETOURNE) {
            throw new IllegalStateException("Le prêt doit être RETOURNE avant d’être clôturé.");
        }

        pret.setStatut(StatutPret.CLOTURE);
        pret.setDateCloture(LocalDateTime.now());
        pret.setCommentaireLecteur(commentaire);

        return pretRepository.save(pret);
    }

    // 4️⃣ ANNULER UNE RÉSERVATION
    public Pret annulerReservation(Long pretId, Utilisateur lecteur) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.RESERVE) {
            throw new IllegalStateException("Seules les réservations peuvent être annulées.");
        }

        if (!pret.getLecteur().getId().equals(lecteur.getId())) {
            throw new SecurityException("Ce prêt ne vous appartient pas.");
        }

        pret.setStatut(StatutPret.ANNULE);
        pret.setDateCloture(LocalDateTime.now());

        return pretRepository.save(pret);
    }

    // 5️⃣ UTILITAIRE
    private Pret getPretOrThrow(Long id) {
        return pretRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prêt introuvable."));
    }
}
