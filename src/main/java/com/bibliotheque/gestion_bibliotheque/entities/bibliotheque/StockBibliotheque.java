package com.bibliotheque.gestion_bibliotheque.entities.bibliotheque;

import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(
    name = "stock_bibliotheque",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"bibliotheque_id", "ressource_id"})
    }
)
public class StockBibliotheque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ////📍 Bibliothèque propriétaire du stock
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bibliotheque_id")
    private Bibliotheque bibliotheque;

    // 📚 Ressource concernée
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ressource_id")
    private Ressource ressource;

    // 📦 Quantité totale possédée
    @Column(nullable = false)
    private int quantiteTotale;

    // ✅ Quantité disponible immédiatement
    @Column(nullable = false)
    private int quantiteDisponible;

    // 🔄 Quantité actuellement empruntée (UTILISÉE POUR LE TAUX DE ROTATION)
    @Column(nullable = false)
    private int quantiteEmpruntee = 0;

    // ⏳ Quantité réservée
    @Column(nullable = false)
    private int quantiteReservee = 0;
}
