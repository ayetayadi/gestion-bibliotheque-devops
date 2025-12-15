package com.bibliotheque.gestion_bibliotheque.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bibliotheque.gestion_bibliotheque.entities.pret.Pret;
import com.bibliotheque.gestion_bibliotheque.entities.pret.StatutPret;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;  // ⭐ IMPORT MANQUANT

public interface PretRepository extends JpaRepository<Pret, Long> {

    // 📌 Tous les prêts / réservations d'un lecteur
    List<Pret> findByLecteur(Utilisateur lecteur);

    // 📌 Pour bibliothécaire : prêts à gérer
    @Query("""
        SELECT p
        FROM Pret p
        WHERE p.bibliotheque = :bibliotheque
          AND p.statut IN :statuts
    """)
    List<Pret> findPretsByBibliothequeAndStatuts(
            @Param("bibliotheque") Bibliotheque bibliotheque,
            @Param("statuts") List<StatutPret> statuts
    );
}
