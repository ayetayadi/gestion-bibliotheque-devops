package com.bibliotheque.gestion_bibliotheque.web.admin;

import com.bibliotheque.gestion_bibliotheque.dao.LogAuditRepository;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.UtilisateurService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final LogAuditRepository logAuditRepository;
    private final UtilisateurService utilisateurService;

    @GetMapping
    public String auditLocal(Model model) {

        Utilisateur admin = utilisateurService.getCurrentUser();

        model.addAttribute(
            "logs",
            logAuditRepository.findByBibliotheque(admin.getBibliotheque())
        );

        return "utilisateur/admin/audit";
    }
}
