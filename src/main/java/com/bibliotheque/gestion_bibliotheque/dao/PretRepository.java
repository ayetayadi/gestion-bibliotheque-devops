package com.bibliotheque.gestion_bibliotheque.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bibliotheque.gestion_bibliotheque.entities.pret.Pret;
import com.bibliotheque.gestion_bibliotheque.entities.pret.StatutPret;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

public interface PretRepository extends JpaRepository<Pret, Long> {

    // ======================
    // USAGER
    // ======================
    List<Pret> findByLecteur(Utilisateur lecteur);

    // ======================
    // BIBLIOTHÉCAIRE
    // ======================
    @Query("""
        SELECT p FROM Pret p
        WHERE p.stockBibliotheque.bibliotheque = :bibliotheque
          AND p.statut IN :statuts
    """)
    List<Pret> findPretsByBibliothequeAndStatuts(
            @Param("bibliotheque") Bibliotheque bibliotheque,
            @Param("statuts") List<StatutPret> statuts
    );

    // ======================
    // ADMIN – KPI
    // ======================

    // 📊 Prêts actifs
    @Query("""
        SELECT COUNT(p)
        FROM Pret p
        WHERE p.statut <> 'CLOTURE'
    """)
    Long countPretsActifs();

    // 📊 Prêts par catégorie
    @Query("""
        SELECT r.categorie, COUNT(p)
        FROM Pret p
        JOIN p.ressource r
        GROUP BY r.categorie
    """)
    List<Object[]> countPretsParCategorie();

    // 📊 Prêts par bibliothèque
    @Query("""
        SELECT b.nom, COUNT(p)
        FROM Pret p
        JOIN p.bibliotheque b
        GROUP BY b.nom
    """)
    List<Object[]> countPretsParBibliotheque();

    // 📊 Activité des utilisateurs
    @Query("""
        SELECT u.email, COUNT(p)
        FROM Pret p
        JOIN p.lecteur u
        GROUP BY u.email
    """)
    List<Object[]> countPretsParUtilisateur();
}
