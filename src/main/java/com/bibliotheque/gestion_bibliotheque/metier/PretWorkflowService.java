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
    private final JavaMailSender mailSender; // Injection de Spring Mail

    // 1️⃣ RÉSERVER UNE RESSOURCE
    public Pret reserverRessource(Utilisateur lecteur, Ressource ressource,
                                  StockBibliotheque stock) {
        if (stock.getQuantiteDisponible() <= 0) {
            throw new IllegalStateException("Aucun exemplaire disponible pour cette ressource.");
        }

        Pret pret = new Pret();
        pret.setLecteur(lecteur);
        pret.setRessource(ressource);
        pret.setStockBibliotheque(stock);
        pret.setDateReservation(LocalDateTime.now());
        pret.setStatut(StatutPret.RESERVE);

        pretRepository.save(pret);

        // Notification email au lecteur
        sendEmail(
                lecteur.getEmail(),
                "Réservation confirmée",
                "Bonjour " + lecteur.getNom() + ",\n\nVotre réservation pour \"" +
                        ressource.getTitre() + "\" a été effectuée avec succès.\n\nMerci,\nBiblioNet"
        );

        return pret;
    }

    // 2️⃣ VALIDER UN EMPRUNT (RESERVE → EMPRUNTE)
    public Pret validerEmprunt(Long pretId, Utilisateur bibliothecaire) {
        Pret pret = getPretOrThrow(pretId);

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
        pretRepository.save(pret);

        // Notification email au lecteur
        sendEmail(
                pret.getLecteur().getEmail(),
                "Votre livre est prêt à être emprunté",
                "Bonjour " + pret.getLecteur().getNom() + ",\n\nLe livre \"" +
                        pret.getRessource().getTitre() + "\" est maintenant prêt à être emprunté.\n\nMerci,\nBiblioNet"
        );

        return pret;
    }

    // 3️⃣ RETOURNER UNE RESSOURCE (EMPRUNTE / EN_COURS → RETOURNE)
    public Pret retournerPret(Long pretId, Utilisateur utilisateur) {
        Pret pret = getPretOrThrow(pretId);

        // Vérification : le lecteur ou le bibliothécaire
        if (!pret.getLecteur().getId().equals(utilisateur.getId())) {
            throw new SecurityException("Vous ne pouvez pas retourner ce prêt.");
        }

        if (pret.getStatut() != StatutPret.EMPRUNTE && pret.getStatut() != StatutPret.EN_COURS) {
            throw new IllegalStateException("Seul un prêt EMPRUNTE ou EN_COURS peut être retourné.");
        }

        StockBibliotheque stock = pret.getStockBibliotheque();
        stock.setQuantiteDisponible(stock.getQuantiteDisponible() + 1);
        stockRepository.save(stock);

        pret.setStatut(StatutPret.RETOURNE);
        pret.setDateRetour(LocalDateTime.now());
        pretRepository.save(pret);

        // Notification email au bibliothécaire
        // Ici on peut notifier tous les bibliothécaires affectés au stock
        // Si tu veux un email unique, tu peux ajouter un email général de la bibliothèque
        sendEmail(
                utilisateur.getEmail(), // utilisateur = bibliothécaire qui effectue le retour
                "Livre retourné",
                "Bonjour,\n\nLe livre \"" + pret.getRessource().getTitre() + "\" a été retourné par " +
                        pret.getLecteur().getNom() + " " + pret.getLecteur().getPrenom() + ".\n\nMerci."
        );

        return pret;
    }

    // 4️⃣ CLÔTURER LE PRÊT (RETOURNE → CLOTURE)
    public Pret cloturerPret(Long pretId, Utilisateur bibliothecaire, String commentaire) {
        Pret pret = getPretOrThrow(pretId);

        if (pret.getStatut() != StatutPret.RETOURNE) {
            throw new IllegalStateException("Le prêt doit être RETOURNE avant d’être clôturé.");
        }

        pret.setStatut(StatutPret.CLOTURE);
        pret.setDateCloture(LocalDateTime.now());
        pret.setCommentaireLecteur(commentaire);
        pretRepository.save(pret);

        // Notification email au lecteur
        sendEmail(
                pret.getLecteur().getEmail(),
                "Prêt clôturé",
                "Bonjour " + pret.getLecteur().getNom() + ",\n\n" +
                        "Votre prêt pour \"" + pret.getRessource().getTitre() + "\" a été clôturé.\n\nMerci,\nBiblioNet"
        );

        return pret;
    }

    // 5️⃣ ANNULER UNE RÉSERVATION (par le lecteur)
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
        pretRepository.save(pret);

        // Notification email au lecteur
        sendEmail(
                lecteur.getEmail(),
                "Réservation annulée",
                "Bonjour " + lecteur.getNom() + ",\n\nVotre réservation pour \"" +
                        pret.getRessource().getTitre() + "\" a été annulée.\n\nMerci,\nBiblioNet"
        );

        return pret;
    }

    // 🔹 Méthode utilitaire pour récupérer un prêt
    private Pret getPretOrThrow(Long id) {
        return pretRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prêt introuvable."));
    }

    // 🔹 Méthode utilitaire pour envoyer les emails
    private void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) return; // ignore si email manquant
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("bannermanagement01@gmail.com"); // email principal
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
