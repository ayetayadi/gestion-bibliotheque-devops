package com.bibliotheque.gestion_bibliotheque.dao;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BibliothequeRepository extends JpaRepository<Bibliotheque, Long> {

    boolean existsByCode(String code);

    List<Bibliotheque> findByActifTrue();

    long countByActifTrue();

    long countByActifFalse();
}
