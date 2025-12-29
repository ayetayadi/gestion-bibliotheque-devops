package com.bibliotheque.gestion_bibliotheque.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bibliotheque.gestion_bibliotheque.entities.notification.Notification;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import java.util.List;
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataireOrderByDateEnvoiDesc(Utilisateur utilisateur);

    //  Vérifier si une notification existe déjà pour cette ressource et ce lecteur
    @Query("""
        SELECT COUNT(n) 
        FROM Notification n
        JOIN Pret p ON p.lecteur = n.destinataire
        WHERE n.destinataire = :lecteur
          AND p.ressource = :ressource
          AND n.vue = false
    """)
    long countNonLuesPourRessource(
            @Param("lecteur") Utilisateur lecteur,
            @Param("ressource") Ressource ressource
    );

    // Récupérer la ressource liée à une notification
    @Query("""
        SELECT r 
        FROM Ressource r
        JOIN Pret p ON p.ressource = r
        JOIN Notification n ON n.destinataire = p.lecteur
        WHERE n.id = :notifId
    """)
    Ressource findRessourceParNotification(Long notifId);


@Query("""
    SELECT n
    FROM Notification n
    WHERE n.destinataire = :lecteur
      AND n.active = true
    ORDER BY n.dateEnvoi DESC
""")
List<Notification> findActives(Utilisateur lecteur);

@Query("""
    SELECT COUNT(n)
    FROM Notification n
    JOIN Pret p ON p.lecteur = n.destinataire
    WHERE n.destinataire = :lecteur
      AND p.ressource = :ressource
      AND n.active = true
""")
long existeNotifActive(Utilisateur lecteur, Ressource ressource);

@Modifying
@Query("""
    DELETE FROM Notification n
    WHERE n.destinataire = :lecteur
      AND n.active = true
      AND EXISTS (
          SELECT p FROM Pret p
          WHERE p.lecteur = n.destinataire
            AND p.ressource = :ressource
            AND p.dateRetour IS NOT NULL
      )
""")
void deleteOnReturn(Utilisateur lecteur, Ressource ressource);

}
