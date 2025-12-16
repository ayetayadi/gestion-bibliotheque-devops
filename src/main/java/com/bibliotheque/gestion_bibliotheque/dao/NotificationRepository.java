package com.bibliotheque.gestion_bibliotheque.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bibliotheque.gestion_bibliotheque.entities.notification.Notification;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByDestinataireOrderByDateEnvoiDesc(Utilisateur utilisateur);
}
