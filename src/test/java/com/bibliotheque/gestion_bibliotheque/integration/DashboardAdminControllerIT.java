package com.bibliotheque.gestion_bibliotheque.integration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.RapportService;
import com.bibliotheque.gestion_bibliotheque.metier.UtilisateurService;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.thymeleaf.enabled=false"
})
class DashboardAdminControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RapportService rapportService;

    @MockBean
    private UtilisateurService utilisateurService;

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void shouldLoadAdminDashboard() throws Exception {

        /* ========== GIVEN ========== */
        Bibliotheque bib = new Bibliotheque();
        bib.setId(1L);

        Utilisateur admin = new Utilisateur();
        admin.setBibliotheque(bib);

        when(utilisateurService.getByEmail("admin@test.com"))
                .thenReturn(admin);

        when(rapportService.totalPretsParBibliotheque(1L)).thenReturn(10L);
        when(rapportService.totalStockParBibliotheque(1L)).thenReturn(100L);
        when(rapportService.tauxRotationParBibliotheque(1L)).thenReturn(10.0);
        when(rapportService.pretsParCategorieParBibliotheque(1L))
                .thenReturn(Map.of());
        when(rapportService.pretsParStatutParBibliotheque(1L))
                .thenReturn(Map.of());

        /* ========== WHEN / THEN ========== */
        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().isOk());
    }
}
