package com.bibliotheque.gestion_bibliotheque.web.admin;

import java.io.IOException;
import java.security.Principal;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.RapportService;
import com.bibliotheque.gestion_bibliotheque.metier.UtilisateurService;
import com.bibliotheque.gestion_bibliotheque.util.PdfExportUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardAdminController {

    private final RapportService rapportService;
    private final UtilisateurService utilisateurService;

    // ======================
    // 📊 DASHBOARD ADMIN
    // ======================
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {

        Utilisateur admin =
                utilisateurService.getByEmail(principal.getName());

        Long bibId = admin.getBibliotheque().getId();

        model.addAttribute("totalPrets",
                rapportService.totalPretsParBibliotheque(bibId));

        model.addAttribute("totalStock",
                rapportService.totalStockParBibliotheque(bibId));

        model.addAttribute("tauxRotation",
                rapportService.tauxRotationParBibliotheque(bibId));

        model.addAttribute("pretsParCategorie",
                rapportService.pretsParCategorieParBibliotheque(bibId));

        model.addAttribute("pretsParStatut",
                rapportService.pretsParStatutParBibliotheque(bibId));

        return "utilisateur/admin/dashboard";
    }

    // ======================
    // 📄 EXPORT PDF
    // ======================
    @GetMapping("/export/rapport/pdf")
    public void exportPdf(HttpServletResponse response, Principal principal)
            throws IOException {

        Utilisateur admin =
                utilisateurService.getByEmail(principal.getName());

        Long bibId = admin.getBibliotheque().getId();

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=rapport_bibliotheque.pdf"
        );

        PdfExportUtil.exportRapportBibliotheque(
                rapportService.totalPretsParBibliotheque(bibId),
                rapportService.totalStockParBibliotheque(bibId),
                rapportService.tauxRotationParBibliotheque(bibId),
                rapportService.pretsParCategorieParBibliotheque(bibId),
                rapportService.pretsParStatutParBibliotheque(bibId),
                response
        );
    }
}
