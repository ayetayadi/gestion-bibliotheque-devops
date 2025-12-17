package com.bibliotheque.gestion_bibliotheque.dao;

import com.bibliotheque.gestion_bibliotheque.entities.log.LogAudit;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAuditRepository extends JpaRepository<LogAudit, Long> {

    // 🔍 Audit local : logs d'une seule bibliothèque
    List<LogAudit> findByBibliotheque(Bibliotheque bibliotheque);

    // (optionnel plus tard)
    // List<LogAudit> findByBibliothequeOrderByDateActionDesc(Bibliotheque bibliotheque);
}
