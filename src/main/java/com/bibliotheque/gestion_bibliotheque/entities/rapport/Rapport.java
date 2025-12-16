package com.bibliotheque.gestion_bibliotheque.entities.rapport;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeRapport type;

    private LocalDate periodeDebut;
    private LocalDate periodeFin;

    @Column(nullable = false)
    private LocalDateTime dateGeneration;

    private String cheminExport;

    @Column(length = 2000)
    private String contenuJson;

    // ✅ Relation avec l'admin
    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Utilisateur generePar;
}
