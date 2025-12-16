package com.bibliotheque.gestion_bibliotheque.entities.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private LocalDateTime dateEnvoi;

    private boolean vue = false; // false si non lue

    @ManyToOne
    private Utilisateur destinataire;
}
