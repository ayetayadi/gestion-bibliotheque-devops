package com.bibliotheque.gestion_bibliotheque.metier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.dao.StockBibliothequeRepository;

@ExtendWith(MockitoExtension.class)
class DashboardBibliothequeServiceTest {

    @Mock
    private PretRepository pretRepository;

    @Mock
    private StockBibliothequeRepository stockRepository;

    @InjectMocks
    private DashboardBibliothequeService dashboardService;

    private Long bibliothequeId;

    @BeforeEach
    void setUp() {
        bibliothequeId = 1L;
    }

    @Test
    void dashboardBibliotheque_shouldReturnDashboardData() {
        /* ================= MOCK PRETS PAR STATUT ================= */
        List<Object[]> pretsParStatut = List.of(
                new Object[]{"EN_COURS", 5L},
                new Object[]{"RETOURNE", 3L}
        );

        when(pretRepository.countPretsParStatutBibliotheque(bibliothequeId))
                .thenReturn(pretsParStatut);

        /* ================= MOCK PRETS PAR CATEGORIE ================= */
        List<Object[]> pretsParCategorie = List.of(
                new Object[]{"LITTERATURE", 4L},
                new Object[]{"SCIENCE", 2L}
        );

        when(pretRepository.countPretsParCategorieBibliotheque(bibliothequeId))
                .thenReturn(pretsParCategorie);

        /* ================= MOCK STOCK ================= */
        when(stockRepository.stockTotalParBibliotheque(bibliothequeId))
                .thenReturn(10L);

        when(stockRepository.stockEmprunteParBibliotheque(bibliothequeId))
                .thenReturn(5L);

        /* ================= CALL SERVICE ================= */
        Map<String, Object> dashboard =
                dashboardService.dashboardBibliotheque(bibliothequeId);

        /* ================= ASSERTIONS ================= */
        assertNotNull(dashboard);

        // Vérification clés principales
        assertTrue(dashboard.containsKey("pretsParStatut"));
        assertTrue(dashboard.containsKey("pretsParCategorie"));
        assertTrue(dashboard.containsKey("stockTotal"));
        assertTrue(dashboard.containsKey("tauxRotation"));

        // Vérification contenu
        Map<String, Long> statutMap =
                (Map<String, Long>) dashboard.get("pretsParStatut");
        assertEquals(5L, statutMap.get("EN_COURS"));
        assertEquals(3L, statutMap.get("RETOURNE"));

        Map<String, Long> categorieMap =
                (Map<String, Long>) dashboard.get("pretsParCategorie");
        assertEquals(4L, categorieMap.get("LITTERATURE"));
        assertEquals(2L, categorieMap.get("SCIENCE"));

        assertEquals(10L, dashboard.get("stockTotal"));
        assertEquals(50.0, (double) dashboard.get("tauxRotation"));
    }

    @Test
    void dashboardBibliotheque_shouldHandleZeroStock() {
        when(pretRepository.countPretsParStatutBibliotheque(bibliothequeId))
                .thenReturn(List.of());

        when(pretRepository.countPretsParCategorieBibliotheque(bibliothequeId))
                .thenReturn(List.of());

        when(stockRepository.stockTotalParBibliotheque(bibliothequeId))
                .thenReturn(0L);

        when(stockRepository.stockEmprunteParBibliotheque(bibliothequeId))
                .thenReturn(0L);

        Map<String, Object> dashboard =
                dashboardService.dashboardBibliotheque(bibliothequeId);

        assertEquals(0.0, (double) dashboard.get("tauxRotation"));
    }
}
