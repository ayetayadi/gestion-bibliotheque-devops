package com.bibliotheque.gestion_bibliotheque.dao;

import java.util.List;  // ✅ LE BON IMPORT

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;

@Repository
public interface RessourceRepository extends JpaRepository<Ressource, Long> {

    List<Ressource> findByBibliotheque(Bibliotheque bibliotheque);
}
