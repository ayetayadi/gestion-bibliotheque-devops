package com.bibliotheque.gestion_bibliotheque.util;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

class PdfExportUtilTest {

    @Test
    void shouldExportPdfWithoutException() {
        // GIVEN
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
                @Override
                public void write(int b) {
                    outputStream.write(b);
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // not needed for test
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, Long> pretsParCategorie = new HashMap<>();
        pretsParCategorie.put("Livre", 5L);

        Map<String, Long> pretsParStatut = new HashMap<>();
        pretsParStatut.put("En cours", 3L);

        // WHEN + THEN
        assertDoesNotThrow(() ->
                PdfExportUtil.exportRapportBibliotheque(
                        10L,
                        20L,
                        50.0,
                        pretsParCategorie,
                        pretsParStatut,
                        response
                )
        );
    }
}
