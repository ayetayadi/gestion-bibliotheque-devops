package com.bibliotheque.gestion_bibliotheque.util;

import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class PdfExportUtil {

    private PdfExportUtil() {}

    public static void exportRapportBibliotheque(
            long totalPrets,
            long totalStock,
            double tauxRotation,
            Map<String, Long> pretsParCategorie,
            Map<String, Long> pretsParStatut,
            HttpServletResponse response
    ) {

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font section = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font text = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("Rapport Bibliothèque", title));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Indicateurs clés", section));
            document.add(new Paragraph("Total des prêts : " + totalPrets, text));
            document.add(new Paragraph("Stock total : " + totalStock, text));
            document.add(new Paragraph(
                    "Taux de rotation : " + String.format("%.2f", tauxRotation) + " %",
                    text
            ));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Prêts par catégorie", section));
            document.add(createTable(pretsParCategorie));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Prêts par statut", section));
            document.add(createTable(pretsParStatut));

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF", e);
        }
    }

    private static PdfPTable createTable(Map<String, Long> data) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        table.addCell("Libellé");
        table.addCell("Valeur");

        data.forEach((k, v) -> {
            table.addCell(k);
            table.addCell(String.valueOf(v));
        });

        return table;
    }
}
