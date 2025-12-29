package com.bibliotheque.gestion_bibliotheque.metier;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bibliotheque.gestion_bibliotheque.dao.NotificationRepository;
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
    private final NotificationRepository notificationRepository;
    private final StockBibliothequeRepository stockRepository;
    private final JavaMailSender mailSender;

    // ======================================================
    // 1️⃣ RÉSERVER
    // ======================================================
    public Pret reserverRessource(
            Utilisateur lecteur,
            Ressource ressource,
            StockBibliotheque stock
    ) {

        log.info("🆕 Réservation : lecteur={}, ressource={}", lecteur.getEmail(), ressource.getId());

        boolean dejaReserve = pretRepository.findByLecteur(lecteur).stream()
                .anyMatch(p ->
                        p.getRessource().getId().equals(ressource.getId()) &&
                                (p.getStatut() == StatutPret.RESERVE
                                        || p.getStatut() == StatutPret.EMPRUNTE
                                        || p.getStatut() == StatutPret.EN_COURS)
                );

        if (dejaReserve) {
            throw new IllegalStateException("Vous avez déjà réservé ou emprunté cette ressource.");
        }

        if (stock.getQuantiteDisponible() <= 0) {
            throw new IllegalStateException("Aucun exemplaire disponible.");
        }

        // 🔄 Mise à jour stock
        stock.setQuantiteDisponible(stock.getQuantiteDisponible() - 1);
        stock.setQuantiteReservee(stock.getQuantiteReservee() + 1);
        stockRepository.save(stock);

        // 📄 Création du prêt
        Pret pret = new Pret();
        pret.setLecteur(lecteur);
        pret.setRessource(ressource);
        pret.setStockBibliotheque(stock);
        pret.setBibliotheque(stock.getBibliotheque()); // important
        pret.setDateReservation(LocalDateTime.now());
        pret.setStatut(StatutPret.RESERVE);

        pretRepository.save(pret);

        // 📩 EMAIL AJOUTÉ
        sendEmail(
                lecteur.getEmail(),
                "Réservation confirmée",
                "Bonjour " + lecteur.getNom()
                        + ",\nVotre réservation pour \"" + ressource.getTitre()
                        + "\" est confirmée.\n\nBiblioNet"
        );

        return pret;
    }

    // ======================================================
    // 2️⃣ VALIDER EMPRUNT
    // ======================================================
    public Pret validerEmprunt(
            Long pretId,
            Utilisateur bibliothecaire,
            String dateDebutStr,
            String dateFinStr
    ) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.RESERVE) {
            throw new IllegalStateException("Le prêt doit être au statut RESERVE.");
        }

        // Parsing
        LocalDateTime dateDebut;
        LocalDateTime dateFin;

        try {
            dateDebut = LocalDateTime.parse(dateDebutStr);
            dateFin = LocalDateTime.parse(dateFinStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Format date invalide (yyyy-MM-ddTHH:mm)");
        }

        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin ne peut pas être avant la date de début.");
        }

        // 🔄 Mise à jour stock
        StockBibliotheque stock = pret.getStockBibliotheque();
        stock.setQuantiteReservee(stock.getQuantiteReservee() - 1);
        stock.setQuantiteEmpruntee(stock.getQuantiteEmpruntee() + 1);
        stockRepository.save(stock);

        // 📄 Mise à jour prêt
        pret.setBibliotheque(stock.getBibliotheque());
        pret.setStatut(StatutPret.EMPRUNTE);
        pret.setDateDebutEmprunt(dateDebut);
        pret.setDateFinPrevu(dateFin);
        pretRepository.save(pret);

        // 📩 EMAIL AJOUTÉ
        sendEmail(
                pret.getLecteur().getEmail(),
                "Votre livre est prêt à être emprunté",
                "Bonjour " + pret.getLecteur().getNom()
                        + ",\n\nLe livre \"" + pret.getRessource().getTitre()
                        + "\" est maintenant prêt à être emprunté.\n\nBiblioNet"
        );

        return pret;
    }

    // ======================================================
    // 3️⃣ RETOURNER
    // ======================================================
    public Pret retournerPret(Long pretId, Utilisateur lecteur) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.EMPRUNTE) {
            throw new IllegalStateException("Le prêt doit être au statut EMPRUNTE.");
        }

        StockBibliotheque stock = pret.getStockBibliotheque();

        stock.setQuantiteEmpruntee(stock.getQuantiteEmpruntee() - 1);
        stock.setQuantiteDisponible(stock.getQuantiteDisponible() + 1);
        stockRepository.save(stock);

        pret.setBibliotheque(stock.getBibliotheque());
        pret.setStatut(StatutPret.RETOURNE);
        pret.setDateRetour(LocalDateTime.now());
        pretRepository.save(pret);

        // 🗑️ Suppression notif retard
        notificationRepository.deleteOnReturn(lecteur, pret.getRessource());

        // 📩 EMAIL AJOUTÉ
        sendEmail(
                lecteur.getEmail(),
                "Livre retourné",
                "Bonjour,\n\nLe livre \"" + pret.getRessource().getTitre()
                        + "\" a bien été retourné.\n\nMerci."
        );

        return pret;
    }

    // ======================================================
    // 4️⃣ CLÔTURER
    // ======================================================
    public Pret cloturerPret(
            Long pretId,
            Utilisateur bibliothecaire,
            String commentaire
    ) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.RETOURNE) {
            throw new IllegalStateException("Le prêt doit être au statut RETOURNE.");
        }

        pret.setBibliotheque(pret.getStockBibliotheque().getBibliotheque());
        pret.setStatut(StatutPret.CLOTURE);
        pret.setDateCloture(LocalDateTime.now());
        pret.setCommentaireLecteur(commentaire);
        pretRepository.save(pret);

        // 📩 EMAIL AJOUTÉ
        sendEmail(
                pret.getLecteur().getEmail(),
                "Prêt clôturé",
                "Bonjour " + pret.getLecteur().getNom()
                        + ",\n\nVotre prêt pour \"" + pret.getRessource().getTitre()
                        + "\" a été clôturé.\n\nMerci,\nBiblioNet"
        );

        return pret;
    }

    // ======================================================
    // 5️⃣ ANNULER RÉSERVATION
    // ======================================================
    public Pret annulerReservation(Long pretId, Utilisateur lecteur) {

        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.RESERVE) {
            throw new IllegalStateException("Seules les réservations peuvent être annulées.");
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
        pretRepository.save(pret);

        // 📩 EMAIL AJOUTÉ
        sendEmail(
                lecteur.getEmail(),
                "Réservation annulée",
                "Bonjour " + lecteur.getNom()
                        + ",\n\nVotre réservation pour \"" + pret.getRessource().getTitre()
                        + "\" a été annulée.\n\nBiblioNet"
        );

        return pret;
    }

    // ======================================================
    // 🔧 UTILITAIRES
    // ======================================================
    private Pret getPretOrThrow(Long id) {
        return pretRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prêt introuvable"));
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
