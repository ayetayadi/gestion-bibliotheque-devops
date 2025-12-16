package com.bibliotheque.gestion_bibliotheque.metier;

import java.time.LocalDateTime;

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
    public Pret reserverRessource(Utilisateur lecteur, Ressource ressource, StockBibliotheque stock) {

        if (stock.getQuantiteDisponible() <= 0) {
            throw new IllegalStateException("Aucun exemplaire disponible.");
        }

        Pret pret = new Pret();
        pret.setLecteur(lecteur);
        pret.setRessource(ressource);
        pret.setStockBibliotheque(stock);
        pret.setDateReservation(LocalDateTime.now());
        pret.setStatut(StatutPret.RESERVE);

        stock.setQuantiteDisponible(stock.getQuantiteDisponible() - 1);
        stock.setQuantiteReservee(stock.getQuantiteReservee() + 1);
        stockRepository.save(stock);

        pretRepository.save(pret);

        sendEmail(
                lecteur.getEmail(),
                "Réservation confirmée",
                "Bonjour " + lecteur.getNom()
                        + ",\n\nVotre réservation pour \""
                        + ressource.getTitre()
                        + "\" a été enregistrée.\n\nBiblioNet"
        );

        return pret;
    }

    // =========================
    // 2️⃣ VALIDER EMPRUNT
    // =========================
    public Pret validerEmprunt(Long pretId, Utilisateur bibliothecaire) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.RESERVE) {
            throw new IllegalStateException("Le prêt doit être RESERVE.");
        }

        StockBibliotheque stock = pret.getStockBibliotheque();

        stock.setQuantiteReservee(stock.getQuantiteReservee() - 1);
        stock.setQuantiteEmpruntee(stock.getQuantiteEmpruntee() + 1);
        stockRepository.save(stock);

        pret.setStatut(StatutPret.EMPRUNTE);
        pret.setDateDebutEmprunt(LocalDateTime.now());
        pret.setDateFinPrevu(LocalDateTime.now().plusDays(14));

        pretRepository.save(pret);

        return pret;
    }

    // =========================
    // 3️⃣ RETOURNER
    // =========================
    public Pret retournerPret(Long pretId, Utilisateur lecteur) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.EMPRUNTE) {
            throw new IllegalStateException("Le prêt doit être EMPRUNTE.");
        }

        StockBibliotheque stock = pret.getStockBibliotheque();
        stock.setQuantiteEmpruntee(stock.getQuantiteEmpruntee() - 1);
        stock.setQuantiteDisponible(stock.getQuantiteDisponible() + 1);
        stockRepository.save(stock);

        pret.setStatut(StatutPret.RETOURNE);
        pret.setDateRetour(LocalDateTime.now());

        return pretRepository.save(pret);
    }

    // =========================
    // 4️⃣ CLOTURER
    // =========================
    public Pret cloturerPret(Long pretId, Utilisateur bibliothecaire, String commentaire) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.RETOURNE) {
            throw new IllegalStateException("Le prêt doit être RETOURNE.");
        }

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
            throw new IllegalStateException("Seules les réservations peuvent être annulées.");
        }

        if (!pret.getLecteur().getId().equals(lecteur.getId())) {
            throw new SecurityException("Action non autorisée.");
        }

        StockBibliotheque stock = pret.getStockBibliotheque();
        stock.setQuantiteReservee(stock.getQuantiteReservee() - 1);
        stock.setQuantiteDisponible(stock.getQuantiteDisponible() + 1);
        stockRepository.save(stock);

        pret.setStatut(StatutPret.ANNULE);
        pret.setDateCloture(LocalDateTime.now());

        return pretRepository.save(pret);
    }

    // =========================
    // UTILITAIRES
    // =========================
    private Pret getPretOrThrow(Long id) {
        return pretRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prêt introuvable"));
    }

    private void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("bannermanagement01@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
