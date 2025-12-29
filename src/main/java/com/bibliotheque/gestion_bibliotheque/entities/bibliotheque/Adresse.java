package com.bibliotheque.gestion_bibliotheque.entities.bibliotheque;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "adresse")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String rue;

    @Column(nullable = false)
    private String ville;

    @Column(nullable = false)
    private String codePostal;

    private String pays;

    private Double latitude;
    private Double longitude;
}
