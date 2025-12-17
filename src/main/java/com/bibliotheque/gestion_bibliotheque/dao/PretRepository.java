package com.bibliotheque.gestion_bibliotheque.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bibliotheque.gestion_bibliotheque.entities.pret.Pret;
import com.bibliotheque.gestion_bibliotheque.entities.pret.StatutPret;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

public interface PretRepository extends JpaRepository<Pret, Long> {

    /* ======================
       📚 LECTEUR
       ====================== */

    List<Pret> findByLecteur(Utilisateur lecteur);

    @Query("""
        SELECT p
        FROM Pret p
        WHERE p.lecteur = :lecteur
          AND p.statut <> 'ANNULE'
        ORDER BY p.dateReservation DESC
    """)
    Page<Pret> findByLecteur(
            @Param("lecteur") Utilisateur lecteur,
            Pageable pageable
    );

    /* ======================
       🏛️ BIBLIOTHÈQUE – LISTE
       ====================== */

    @Query("""
        SELECT p
        FROM Pret p
        WHERE p.stockBibliotheque.bibliotheque = :bibliotheque
          AND p.statut IN :statuts
    """)
    Page<Pret> findPretsByBibliothequeAndStatuts(
            @Param("bibliotheque") Bibliotheque bibliotheque,
            @Param("statuts") List<StatutPret> statuts,
            Pageable pageable
    );

    /* ======================
       🔎 RECHERCHE AVANCÉE
       ====================== */

    @Query("""
        SELECT p
        FROM Pret p
        WHERE p.stockBibliotheque.bibliotheque.id = :biblioId
          AND (
               :keyword IS NULL OR
               LOWER(p.lecteur.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(p.lecteur.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(p.ressource.titre) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
          AND (:statut IS NULL OR p.statut = :statut)
          AND (:dateMin IS NULL OR p.dateReservation >= :dateMin)
          AND (:dateMax IS NULL OR p.dateReservation <= :dateMax)
    """)
    Page<Pret> searchPrets(
            @Param("biblioId") Long bibliothequeId,
            @Param("keyword") String keyword,
            @Param("statut") StatutPret statut,
            @Param("dateMin") LocalDateTime dateMin,
            @Param("dateMax") LocalDateTime dateMax,
            Pageable pageable
    );

    /* ======================
       📊 KPI – GLOBAL (SUPER ADMIN)
       ====================== */

    @Query("""
        SELECT COUNT(p)
        FROM Pret p
        WHERE p.statut <> 'ANNULE'
    """)
    Long countPretsActifs();

    @Query("""
        SELECT r.categorie, COUNT(p)
        FROM Pret p
        JOIN p.ressource r
        WHERE p.statut <> 'ANNULE'
        GROUP BY r.categorie
    """)
    List<Object[]> countPretsParCategorie();

    @Query("""
        SELECT b.nom, COUNT(p)
        FROM Pret p
        JOIN p.stockBibliotheque sb
        JOIN sb.bibliotheque b
        WHERE p.statut <> 'ANNULE'
        GROUP BY b.nom
    """)
    List<Object[]> countPretsParBibliotheque();

    @Query("""
        SELECT u.email, COUNT(p)
        FROM Pret p
        JOIN p.lecteur u
        WHERE p.statut <> 'ANNULE'
        GROUP BY u.email
    """)
    List<Object[]> countPretsParUtilisateur();

    /* ======================
       🏛️ KPI – PAR BIBLIOTHÈQUE (ADMIN BIB)
       ====================== */

    @Query("""
        SELECT p.statut, COUNT(p)
        FROM Pret p
        WHERE p.stockBibliotheque.bibliotheque.id = :bibId
        GROUP BY p.statut
    """)
    List<Object[]> countPretsParStatutBibliotheque(
            @Param("bibId") Long bibliothequeId
    );

    @Query("""
        SELECT r.categorie, COUNT(p)
        FROM Pret p
        JOIN p.ressource r
        WHERE p.stockBibliotheque.bibliotheque.id = :bibId
          AND p.statut <> 'ANNULE'
        GROUP BY r.categorie
    """)
    List<Object[]> countPretsParCategorieBibliotheque(
            @Param("bibId") Long bibliothequeId
    );

    @Query("""
        SELECT COUNT(p)
        FROM Pret p
        WHERE p.stockBibliotheque.bibliotheque.id = :bibId
          AND p.statut <> 'ANNULE'
    """)
    Long countPretsActifsBibliotheque(
            @Param("bibId") Long bibliothequeId
    );
    
    
    /* ======================
    ⭐ RECOMMANDATION
    ====================== */

 @Query("""
     SELECT p.ressource.categorie, COUNT(p)
     FROM Pret p
     WHERE p.lecteur = :lecteur
       AND p.statut <> 'ANNULE'
     GROUP BY p.ressource.categorie
     ORDER BY COUNT(p) DESC
 """)
 List<Object[]> getTopCategoriesByLecteur(
         @Param("lecteur") Utilisateur lecteur
 );

}
