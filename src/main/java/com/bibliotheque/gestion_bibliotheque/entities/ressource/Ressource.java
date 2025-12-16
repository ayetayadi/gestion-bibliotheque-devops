package com.bibliotheque.gestion_bibliotheque.entities.ressource;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Entity
@Table(name = "ressource")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "dtype",              
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("GENERIC")       
@Getter @Setter
public class Ressource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= COMMUN =================
    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String auteur;

    @Column(unique = true, nullable = true)
    private String isbn;

    @Column(length = 1000)
    private String description;

    private LocalDate datePublication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCategorie categorie;

    private String cheminCouverture;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_ressource", nullable = false)
    private TypeRessource typeRessource;

    // ================= RELATION =================
    @ManyToOne
    @JoinColumn(name = "bibliotheque_id", nullable = false)
    private Bibliotheque bibliotheque;

    // ================= CHAMPS SPÉCIFIQUES =================

    // Livre
    private Integer nbPages;
    private String editeur;

    // Media
    private Integer dureeMinutes;
    private String typeMedia;

    // Document numérique
    private String formatFichier;
    private Double tailleMo;
    private String urlAcces;

    @Enumerated(EnumType.STRING)
    private StatutRessource statut;
}
