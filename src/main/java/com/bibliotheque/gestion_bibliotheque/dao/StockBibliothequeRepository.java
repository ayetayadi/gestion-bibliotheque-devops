package com.bibliotheque.gestion_bibliotheque.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;

public interface StockBibliothequeRepository extends JpaRepository<StockBibliotheque, Long> {

    Optional<StockBibliotheque> findByRessource(Ressource ressource);
}
