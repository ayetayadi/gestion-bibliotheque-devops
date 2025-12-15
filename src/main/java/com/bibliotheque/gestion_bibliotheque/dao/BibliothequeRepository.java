package com.bibliotheque.gestion_bibliotheque.dao;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BibliothequeRepository extends JpaRepository<Bibliotheque, Long> {

    boolean existsByCode(String code);
    List<Bibliotheque> findByActifTrue();
}
