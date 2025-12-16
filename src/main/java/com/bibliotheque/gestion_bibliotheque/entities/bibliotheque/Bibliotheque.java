package com.bibliotheque.gestion_bibliotheque.entities.bibliotheque;

import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "bibliotheque")
@Data
@NoArgsConstructor
public class Bibliotheque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String nom;

    private String telephone;

   

    private boolean actif = true;

    /* ================= RELATIONS ================= */

    // 1 Bibliothèque → 1 Adresse
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "adresse_id", nullable = false)
    private Adresse adresse;

    // 1 Bibliothèque → * Utilisateurs
    @OneToMany(mappedBy = "bibliotheque")
    private List<Utilisateur> utilisateurs;

    // 1 Bibliothèque → * Stocks
    @OneToMany(mappedBy = "bibliotheque", cascade = CascadeType.ALL)
    private List<StockBibliotheque> stock;
}
