package com.bibliotheque.gestion_bibliotheque.entities.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;

@Entity
@Table(name = "utilisateur")
@Data
@NoArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(nullable = false)
    private LocalDateTime dateInscription = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "bibliotheque_id")
    private Bibliotheque bibliotheque;

    @Column(name = "photo")
    private String photo; // ex: "profile_12.jpg"

}
