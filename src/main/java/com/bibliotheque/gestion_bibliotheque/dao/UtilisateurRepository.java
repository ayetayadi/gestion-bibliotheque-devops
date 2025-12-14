package com.bibliotheque.gestion_bibliotheque.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.entities.user.Role;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);
    Page<Utilisateur> findByRole(Role role, Pageable pageable);
    boolean existsByEmail(String email);
    Page<Utilisateur> findByBibliothequeId(Long id, Pageable pageable);
}
