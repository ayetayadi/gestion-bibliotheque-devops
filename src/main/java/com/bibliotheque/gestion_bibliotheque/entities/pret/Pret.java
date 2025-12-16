package com.bibliotheque.gestion_bibliotheque.entities.pret;

import java.time.LocalDateTime;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Data
public class Pret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Utilisateur lecteur;

    @ManyToOne
    private Ressource ressource;

    @ManyToOne
    private Bibliotheque bibliotheque;

    private LocalDateTime dateReservation;
    private LocalDateTime dateDebutEmprunt;
    private LocalDateTime dateFinPrevu;
    private LocalDateTime dateRetour;
    private LocalDateTime dateCloture;

    private String commentaireLecteur;

    @Enumerated(EnumType.STRING)
    private StatutPret statut;

    @ManyToOne
    private StockBibliotheque stockBibliotheque;
}
