package com.bibliotheque.gestion_bibliotheque.metier;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.dao.StockBibliothequeRepository;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.pret.Pret;
import com.bibliotheque.gestion_bibliotheque.entities.pret.StatutPret;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PretWorkflowService {

    private final PretRepository pretRepository;
    private final StockBibliothequeRepository stockRepository;
    private final JavaMailSender mailSender;

    // =========================
    // 1️⃣ RÉSERVER
    // =========================
    public Pret reserverRessource(
            Utilisateur lecteur,
            Ressource ressource,
            StockBibliotheque stock
    ) {

        log.info("🆕 Réservation : lecteur={}, ressource={}",
                lecteur.getEmail(), ressource.getId());

        boolean dejaReserve = pretRepository.findByLecteur(lecteur).stream()
                .anyMatch(p ->
                        p.getRessource().getId().equals(ressource.getId()) &&
                        (p.getStatut() == StatutPret.RESERVE
                                || p.getStatut() == StatutPret.EMPRUNTE
                                || p.getStatut() == StatutPret.EN_COURS)
                );

        if (dejaReserve) {
            throw new IllegalStateException(
                    "Vous avez déjà réservé ou emprunté cette ressource."
            );
        }

        if (stock.getQuantiteDisponible() <= 0) {
            throw new IllegalStateException("Aucun exemplaire disponible.");
        }

        // 🔄 Stock
        stock.setQuantiteDisponible(stock.getQuantiteDisponible() - 1);
        stock.setQuantiteReservee(stock.getQuantiteReservee() + 1);
        stockRepository.save(stock);

        // 📄 Prêt
        Pret pret = new Pret();
        pret.setLecteur(lecteur);
        pret.setRessource(ressource);
        pret.setStockBibliotheque(stock);
        pret.setBibliotheque(stock.getBibliotheque()); // ⭐ FIX CRUCIAL
        pret.setDateReservation(LocalDateTime.now());
        pret.setStatut(StatutPret.RESERVE);

        pretRepository.save(pret);

        sendEmail(
                lecteur.getEmail(),
                "Réservation confirmée",
                "Bonjour " + lecteur.getNom()
                        + ",\nVotre réservation pour \""
                        + ressource.getTitre()
                        + "\" est confirmée.\n\nBiblioNet"
        );

        return pret;
    }

    // =========================
    // 2️⃣ VALIDER EMPRUNT
    // =========================
    public Pret validerEmprunt(Long pretId, Utilisateur bibliothecaire) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.RESERVE) {
            throw new IllegalStateException(
                    "Le prêt doit être au statut RESERVE."
            );
        }

        StockBibliotheque stock = pret.getStockBibliotheque();

        stock.setQuantiteReservee(stock.getQuantiteReservee() - 1);
        stock.setQuantiteEmpruntee(stock.getQuantiteEmpruntee() + 1);
        stockRepository.save(stock);

        pret.setBibliotheque(stock.getBibliotheque()); // ⭐ sécurité
        pret.setStatut(StatutPret.EMPRUNTE);
        pret.setDateDebutEmprunt(LocalDateTime.now());
        pret.setDateFinPrevu(LocalDateTime.now().plusDays(14));

        return pretRepository.save(pret);
    }

    // =========================
    // 3️⃣ RETOURNER
    // =========================
    public Pret retournerPret(Long pretId, Utilisateur lecteur) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.EMPRUNTE) {
            throw new IllegalStateException(
                    "Le prêt doit être au statut EMPRUNTE."
            );
        }

        StockBibliotheque stock = pret.getStockBibliotheque();

        stock.setQuantiteEmpruntee(stock.getQuantiteEmpruntee() - 1);
        stock.setQuantiteDisponible(stock.getQuantiteDisponible() + 1);
        stockRepository.save(stock);

        pret.setBibliotheque(stock.getBibliotheque());
        pret.setStatut(StatutPret.RETOURNE);
        pret.setDateRetour(LocalDateTime.now());

        return pretRepository.save(pret);
    }

    // =========================
    // 4️⃣ CLOTURER
    // =========================
    public Pret cloturerPret(
            Long pretId,
            Utilisateur bibliothecaire,
            String commentaire
    ) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.RETOURNE) {
            throw new IllegalStateException(
                    "Le prêt doit être au statut RETOURNE."
            );
        }

        pret.setBibliotheque(pret.getStockBibliotheque().getBibliotheque());
        pret.setStatut(StatutPret.CLOTURE);
        pret.setDateCloture(LocalDateTime.now());
        pret.setCommentaireLecteur(commentaire);

        return pretRepository.save(pret);
    }

    // =========================
    // 5️⃣ ANNULER RÉSERVATION
    // =========================
    public Pret annulerReservation(Long pretId, Utilisateur lecteur) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.RESERVE) {
            throw new IllegalStateException(
                    "Seules les réservations peuvent être annulées."
            );
        }

        if (!pret.getLecteur().getId().equals(lecteur.getId())) {
            throw new SecurityException("Action interdite.");
        }

        StockBibliotheque stock = pret.getStockBibliotheque();

        stock.setQuantiteReservee(stock.getQuantiteReservee() - 1);
        stock.setQuantiteDisponible(stock.getQuantiteDisponible() + 1);
        stockRepository.save(stock);

        pret.setBibliotheque(stock.getBibliotheque());
        pret.setStatut(StatutPret.ANNULE);
        pret.setDateCloture(LocalDateTime.now());

        return pretRepository.save(pret);
    }

    // =========================
    // 🔎 RECHERCHE
    // =========================
    public Page<Pret> searchPretsBibliotheque(
            Long biblioId,
            String keyword,
            String statut,
            String dateDebut,
            String dateFin,
            Pageable pageable
    ) {

        if (keyword != null && keyword.isBlank()) keyword = null;
        if (statut != null && statut.isBlank()) statut = null;

        LocalDateTime dateMin = null;
        LocalDateTime dateMax = null;

        try {
            if (dateDebut != null && !dateDebut.isBlank())
                dateMin = LocalDateTime.parse(dateDebut);
            if (dateFin != null && !dateFin.isBlank())
                dateMax = LocalDateTime.parse(dateFin);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Format date invalide (yyyy-MM-ddTHH:mm)"
            );
        }

        return pretRepository.searchPrets(
                biblioId,
                keyword,
                statut != null ? StatutPret.valueOf(statut) : null,
                dateMin,
                dateMax,
                pageable
        );
    }

    // =========================
    // 🔧 UTIL
    // =========================
    private Pret getPretOrThrow(Long id) {
        return pretRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Prêt introuvable")
                );
    }

    private void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) return;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("bannermanagement01@gmail.com");
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        mailSender.send(msg);
    }
}
