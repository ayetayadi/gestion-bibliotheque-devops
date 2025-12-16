package com.bibliotheque.gestion_bibliotheque.util;

import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PdfExportUtil {

    private PdfExportUtil() {
        // util class
    }

    // =====================================================
    // 📄 RAPPORT GLOBAL PDF
    // =====================================================
    public static void exportRapportGlobal(
            long totalPrets,
            long pretsActifs,
            long totalStock,
            double tauxRotationGlobal,
            Map<String, Long> pretsParCategorie,
            Map<String, Long> pretsParBibliotheque,
            Map<String, Double> tauxRotationParBibliotheque,
            HttpServletResponse response
    ) {

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, response.getOutputStream());

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            // ================= TITRE =================
            Paragraph title = new Paragraph("Rapport Global - Bibliothèque", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // ================= KPI =================
            document.add(new Paragraph("Indicateurs Clés", sectionFont));
            document.add(new Paragraph("Total des prêts : " + totalPrets, textFont));
            document.add(new Paragraph("Prêts actifs : " + pretsActifs, textFont));
            document.add(new Paragraph("Stock total : " + totalStock, textFont));
            document.add(new Paragraph(
                    "Taux de rotation global : " + String.format("%.2f", tauxRotationGlobal) + " %",
                    textFont
            ));
            document.add(new Paragraph(" "));

            // ================= TABLE 1 =================
            document.add(new Paragraph("Prêts par catégorie", sectionFont));
            document.add(createTableLong(pretsParCategorie, "Catégorie", "Nombre de prêts"));
            document.add(new Paragraph(" "));

            // ================= TABLE 2 =================
            document.add(new Paragraph("Prêts par bibliothèque", sectionFont));
            document.add(createTableLong(pretsParBibliotheque, "Bibliothèque", "Nombre de prêts"));
            document.add(new Paragraph(" "));

            // ================= TABLE 3 =================
            document.add(new Paragraph("Taux de rotation par bibliothèque", sectionFont));
            document.add(createTableDouble(tauxRotationParBibliotheque, "Bibliothèque", "Taux (%)"));

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    // =====================================================
    // 🔧 TABLE LONG
    // =====================================================
    private static PdfPTable createTableLong(
            Map<String, Long> data,
            String col1,
            String col2
    ) {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        table.addCell(header(col1));
        table.addCell(header(col2));

        data.forEach((key, value) -> {
            table.addCell(new PdfPCell(new Phrase(key)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(value))));
        });

        return table;
    }

    // =====================================================
    // 🔧 TABLE DOUBLE
    // =====================================================
    private static PdfPTable createTableDouble(
            Map<String, Double> data,
            String col1,
            String col2
    ) {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        table.addCell(header(col1));
        table.addCell(header(col2));

        data.forEach((key, value) -> {
            table.addCell(new PdfPCell(new Phrase(key)));
            table.addCell(new PdfPCell(
                    new Phrase(String.format("%.2f %%", value))
            ));
        });

        return table;
    }

    private static PdfPCell header(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}
