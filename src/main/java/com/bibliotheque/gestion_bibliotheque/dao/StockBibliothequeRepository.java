package com.bibliotheque.gestion_bibliotheque.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
public interface StockBibliothequeRepository extends JpaRepository<StockBibliotheque, Long> {

    Optional<StockBibliotheque> findByRessource(Ressource ressource);

    /* ===================== */
    /* 🔹 GLOBAL (SUPER ADMIN) */
    /* ===================== */

    @Query("""
        SELECT COALESCE(SUM(s.quantiteTotale), 0)
        FROM StockBibliotheque s
    """)
    Long totalStock();

    @Query("""
        SELECT COALESCE(SUM(s.quantiteEmpruntee), 0)
        FROM StockBibliotheque s
    """)
    Long totalStockEmprunte();

    @Query("""
        SELECT s.bibliotheque.nom,
               COALESCE(SUM(s.quantiteEmpruntee), 0),
               COALESCE(SUM(s.quantiteTotale), 0)
        FROM StockBibliotheque s
        GROUP BY s.bibliotheque.nom
    """)
    List<Object[]> tauxRotationParBibliotheque();

    /* ===================== */
    /* 🏛️ PAR BIBLIOTHÈQUE */
    /* ===================== */

    @Query("""
        SELECT COALESCE(SUM(s.quantiteTotale), 0)
        FROM StockBibliotheque s
        WHERE s.bibliotheque.id = :bibId
    """)
    Long stockTotalParBibliotheque(Long bibId);

    @Query("""
        SELECT COALESCE(SUM(s.quantiteEmpruntee), 0)
        FROM StockBibliotheque s
        WHERE s.bibliotheque.id = :bibId
    """)
    Long stockEmprunteParBibliotheque(Long bibId);
}
