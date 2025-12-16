package com.bibliotheque.gestion_bibliotheque.dao;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BibliothequeRepository extends JpaRepository<Bibliotheque, Long> {

    boolean existsByCode(String code);
    List<Bibliotheque> findByActifTrue();

    @Query("""
    SELECT b FROM Bibliotheque b
    WHERE (:keyword IS NULL OR 
           LOWER(b.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.adresse.ville) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:actif IS NULL OR b.actif = :actif)
""")
Page<Bibliotheque> search(
        @Param("keyword") String keyword,
        @Param("actif") Boolean actif,
        Pageable pageable
);

}
