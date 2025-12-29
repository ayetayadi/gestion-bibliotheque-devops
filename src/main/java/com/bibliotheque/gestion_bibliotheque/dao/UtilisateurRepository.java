package com.bibliotheque.gestion_bibliotheque.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    Page<Utilisateur> findByRoleAndBibliothequeId(
        Role role,
        Long bibliothequeId,
        Pageable pageable
        );

    List<Utilisateur> findByBibliothequeId(Long bibliothequeId);

    @Query("""
    SELECT u FROM Utilisateur u
    WHERE u.role = com.bibliotheque.gestion_bibliotheque.entities.user.Role.ADMIN
      AND (:keyword IS NULL OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))
                         OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))
                         OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:biblioId IS NULL OR u.bibliotheque.id = :biblioId)
      AND (:statut IS NULL OR u.actif = :statut)
    """)
Page<Utilisateur> searchAdmins(
        @Param("keyword") String keyword,
        @Param("biblioId") Long biblioId,
        @Param("statut") Boolean statut,
        Pageable pageable
);

@Query("""
    SELECT u FROM Utilisateur u
    WHERE u.role = com.bibliotheque.gestion_bibliotheque.entities.user.Role.BIBLIOTHECAIRE
      AND u.bibliotheque.id = :biblioId
      AND (:keyword IS NULL OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:statut IS NULL OR u.actif = :statut)
""")
Page<Utilisateur> searchBibliothecaires(
        @Param("biblioId") Long biblioId,
        @Param("keyword") String keyword,
        @Param("statut") Boolean statut,
        Pageable pageable
);


}
