package com.bibliotheque.gestion_bibliotheque.entities.log;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.user.Role;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_audit")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 📅 Date de l'action
    @Column(nullable = false)
    private LocalDateTime dateAction;

    // 👤 Acteur de l'action
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acteur_id")
    private Utilisateur acteur;

    // 🏷️ Rôle de l'acteur (réutilise enum existant)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 🔧 Action métier
    @Column(nullable = false)
    private String action;
    // ex: AJOUT_RESSOURCE, MODIF_STOCK, VALIDER_PRET

    // 🎯 Cible de l'action
    @Column(length = 255)
    private String cible;
    // ex: ISBN, idRessource, idPret

    // ✅ Résultat
    @Column(nullable = false)
    private String resultat;
    // SUCCESS / FAIL

    // 🏛️ Bibliothèque concernée (audit local)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bibliotheque_id")
    private Bibliotheque bibliotheque;
    
    @Column(length = 2000)
    private String details; // ex: "titre=Clean Code; auteur=Robert Martin; isbn=..."

}
