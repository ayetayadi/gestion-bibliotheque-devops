package com.bibliotheque.gestion_bibliotheque.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bibliotheque.gestion_bibliotheque.entities.rapport.Rapport;
import com.bibliotheque.gestion_bibliotheque.entities.rapport.TypeRapport;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

public interface RapportRepository extends JpaRepository<Rapport, Long> {

    // 📄 Tous les rapports d’un admin
    List<Rapport> findByGenerePar(Utilisateur admin);

    // 📊 Rapports par type (ex : PRETS_PAR_CATEGORIE)
    List<Rapport> findByType(TypeRapport type);

    // 🕒 Derniers rapports (utile pour dashboard / historique)
    List<Rapport> findTop10ByOrderByDateGenerationDesc();
}
