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
 List<Pret> findByLecteur(Utilisateur lecteur);
   @Query("""
    SELECT p FROM Pret p
    WHERE p.lecteur = :lecteur
      AND p.statut <> 'ANNULE'
    ORDER BY p.dateReservation DESC
""")
Page<Pret> findByLecteur(@Param("lecteur") Utilisateur lecteur, Pageable pageable);


   @Query("""
    SELECT p FROM Pret p
    WHERE p.stockBibliotheque.bibliotheque = :bibliotheque
      AND p.statut IN :statuts
""")
Page<Pret> findPretsByBibliothequeAndStatuts(
        @Param("bibliotheque") Bibliotheque bibliotheque,
        @Param("statuts") List<StatutPret> statuts,
        Pageable pageable
);


    List<Pret> findByLecteur(Utilisateur lecteur);

    @Query("""
        SELECT p.ressource.categorie, COUNT(p)
        FROM Pret p
        WHERE p.lecteur = :lecteur
          AND p.statut <> 'ANNULE'
        GROUP BY p.ressource.categorie
        ORDER BY COUNT(p) DESC
    """)

    List<Object[]> getTopCategoriesByLecteur(@Param("lecteur") Utilisateur lecteur);

@Query("""
    SELECT p FROM Pret p
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
