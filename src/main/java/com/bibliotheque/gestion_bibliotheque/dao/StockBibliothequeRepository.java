package com.bibliotheque.gestion_bibliotheque.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;

public interface StockBibliothequeRepository extends JpaRepository<StockBibliotheque, Long> {

    // 🔎 Trouver le stock d’une ressource (toutes bibliothèques confondues)
    Optional<StockBibliotheque> findByRessource(Ressource ressource);

    // =========================
    // 📊 DASHBOARD — KPI GLOBAUX
    // =========================

    // 📦 Stock total réseau
    @Query("""
        SELECT COALESCE(SUM(s.quantiteTotale), 0)
        FROM StockBibliotheque s
    """)
    Long totalStock();

    // 🔄 Total emprunté (clé du taux rotation global)
    @Query("""
        SELECT COALESCE(SUM(s.quantiteEmpruntee), 0)
        FROM StockBibliotheque s
    """)
    Long totalStockEmprunte();

    // =========================
    // 📊 DASHBOARD — PAR BIBLIOTHÈQUE
    // =========================

    /*
     * row[0] = nom bibliothèque
     * row[1] = quantité empruntée
     * row[2] = quantité totale
     */
    @Query("""
        SELECT s.bibliotheque.nom,
               COALESCE(SUM(s.quantiteEmpruntee), 0),
               COALESCE(SUM(s.quantiteTotale), 0)
        FROM StockBibliotheque s
        GROUP BY s.bibliotheque.nom
    """)
    List<Object[]> tauxRotationParBibliotheque();
}
