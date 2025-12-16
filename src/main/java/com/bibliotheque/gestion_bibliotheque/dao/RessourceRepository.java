package com.bibliotheque.gestion_bibliotheque.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeCategorie;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeRessource;

import java.util.List;

@Repository
public interface RessourceRepository extends JpaRepository<Ressource, Long> {

    List<Ressource> findByBibliotheque(Bibliotheque bibliotheque);

    @Query("""
        SELECT r FROM Ressource r
        WHERE (:keyword IS NULL OR LOWER(r.titre) LIKE LOWER(CONCAT('%', :keyword, '%'))
                               OR LOWER(r.auteur) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categorie IS NULL OR r.categorie = :categorie)
          AND (:typeRessource IS NULL OR r.typeRessource = :typeRessource)
          AND (:biblioId IS NULL OR r.bibliotheque.id = :biblioId)
    """)
    Page<Ressource> searchCatalogue(
            @Param("keyword") String keyword,
            @Param("categorie") TypeCategorie categorie,
            @Param("typeRessource") TypeRessource typeRessource,
            @Param("biblioId") Long biblioId,
            Pageable pageable
    );
}
