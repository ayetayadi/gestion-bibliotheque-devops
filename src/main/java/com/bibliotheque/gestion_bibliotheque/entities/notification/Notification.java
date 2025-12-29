package com.bibliotheque.gestion_bibliotheque.entities.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private LocalDateTime dateEnvoi;

    private boolean vue = false; // false si non lue

    // Notification toujours active tant que prêt non rendu
    private boolean active = true;

    @ManyToOne
    private Utilisateur destinataire;
}
