package com.bibliotheque.gestion_bibliotheque.metier;


import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.bibliotheque.gestion_bibliotheque.dao.NotificationRepository;
import com.bibliotheque.gestion_bibliotheque.entities.notification.Notification;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

@Service
@RequiredArgsConstructor
public class NotificationWSService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    public void notifyLecteur(Utilisateur lecteur, Ressource ressource, String message) {

        long existe = notificationRepository.existeNotifActive(lecteur, ressource);

        // Si notif existe déjà → ne pas recréer
        if (existe > 0) {
            return;
        }

        Notification n = new Notification();
        n.setDestinataire(lecteur);
        n.setMessage(message);
        n.setActive(true);
        n.setDateEnvoi(LocalDateTime.now());

        notificationRepository.save(n);

        // Envoi WebSocket
        messagingTemplate.convertAndSendToUser(
                lecteur.getEmail(),
                "/topic/notifications",
                message
        );
    }
}
