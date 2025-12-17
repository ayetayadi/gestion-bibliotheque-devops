package com.bibliotheque.gestion_bibliotheque.metier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.dao.StockBibliothequeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardBibliothequeService {

    private final PretRepository pretRepository;
    private final StockBibliothequeRepository stockRepository;

    /**
     * 📊 Dashboard KPI par bibliothèque
     */
    public Map<String, Object> dashboardBibliotheque(Long bibliothequeId) {

        Map<String, Object> dashboard = new HashMap<>();

        /* ================= PRÊTS PAR STATUT ================= */
        Map<String, Long> pretsParStatut = new HashMap<>();

        List<Object[]> dataStatut =
                pretRepository.countPretsParStatutBibliotheque(bibliothequeId);

        for (Object[] row : dataStatut) {
            String statut = String.valueOf(row[0]);
            Long nb = (Long) row[1];
            pretsParStatut.put(statut, nb);
        }

        /* ================= PRÊTS PAR CATÉGORIE ================= */
        Map<String, Long> pretsParCategorie = new HashMap<>();

        List<Object[]> dataCategorie =
                pretRepository.countPretsParCategorieBibliotheque(bibliothequeId);

        for (Object[] row : dataCategorie) {
            String categorie = String.valueOf(row[0]);
            Long nb = (Long) row[1];
            pretsParCategorie.put(categorie, nb);
        }

        /* ================= STOCK ================= */
        Long stockTotal =
                stockRepository.stockTotalParBibliotheque(bibliothequeId);

        Long stockEmprunte =
                stockRepository.stockEmprunteParBibliotheque(bibliothequeId);

        double tauxRotation = 0.0;
        if (stockTotal != null && stockTotal > 0) {
            tauxRotation = (double) stockEmprunte * 100 / stockTotal;
        }

        /* ================= EXPORT ================= */
        dashboard.put("pretsParStatut", pretsParStatut);
        dashboard.put("pretsParCategorie", pretsParCategorie);
        dashboard.put("stockTotal", stockTotal);
        dashboard.put("tauxRotation", tauxRotation);

        return dashboard;
    }
}
