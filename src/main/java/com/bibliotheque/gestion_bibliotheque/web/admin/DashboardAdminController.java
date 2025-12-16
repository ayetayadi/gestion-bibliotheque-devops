package com.bibliotheque.gestion_bibliotheque.web.admin;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bibliotheque.gestion_bibliotheque.metier.RapportService;
import com.bibliotheque.gestion_bibliotheque.util.PdfExportUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardAdminController {

    private final RapportService rapportService;

    // ======================
    // 📊 DASHBOARD ADMIN
    // ======================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // KPI
        model.addAttribute("totalPrets", rapportService.totalPrets());
        model.addAttribute("pretsActifs", rapportService.totalPretsActifs());
        model.addAttribute("totalStock", rapportService.totalStock());
        model.addAttribute("tauxRotationGlobal", rapportService.tauxRotationGlobal());

        // Graphiques
        model.addAttribute("pretsParCategorie", rapportService.pretsParCategorie());
        model.addAttribute("pretsParBibliotheque", rapportService.pretsParBibliotheque());
        model.addAttribute("tauxRotationParBibliotheque", rapportService.tauxRotationParBibliotheque());

        return "utilisateur/admin/dashboard";
    }

    // ======================
    // 📄 EXPORT RAPPORT GLOBAL PDF
    // ======================
    @GetMapping("/export/rapport/pdf")
    public void exportRapportGlobalPdf(HttpServletResponse response)
            throws IOException {

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=rapport_global_bibliotheque.pdf"
        );

        PdfExportUtil.exportRapportGlobal(
                rapportService.totalPrets(),
                rapportService.totalPretsActifs(),
                rapportService.totalStock(),
                rapportService.tauxRotationGlobal(),
                rapportService.pretsParCategorie(),
                rapportService.pretsParBibliotheque(),
                rapportService.tauxRotationParBibliotheque(),
                response
        );
    }
}
