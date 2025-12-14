package com.bibliotheque.gestion_bibliotheque.entities.bibliotheque;

import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class StockBibliotheque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Bibliotheque bibliotheque;

    @ManyToOne(optional = false)
    private Ressource ressource;

    // Quantité totale possédée par la bibliothèque
    @Column(nullable = false)
    private int quantiteTotale;

    // Quantité actuellement disponible (non empruntée et non réservée)
    @Column(nullable = false)
    private int quantiteDisponible;

    // Quantité actuellement empruntée
    @Column(nullable = false)
    private int quantiteEmpruntee = 0;

    // Quantité réservée par des utilisateurs
    @Column(nullable = false)
    private int quantiteReservee = 0;
}
