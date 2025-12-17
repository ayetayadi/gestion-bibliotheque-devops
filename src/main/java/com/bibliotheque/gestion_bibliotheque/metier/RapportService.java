package com.bibliotheque.gestion_bibliotheque.metier;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.dao.RapportRepository;
import com.bibliotheque.gestion_bibliotheque.dao.StockBibliothequeRepository;
import com.bibliotheque.gestion_bibliotheque.entities.rapport.Rapport;
import com.bibliotheque.gestion_bibliotheque.entities.rapport.TypeRapport;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RapportService {

    private final PretRepository pretRepository;
    private final StockBibliothequeRepository stockRepository;
    private final RapportRepository rapportRepository;

    /* =====================================================
       📊 KPI — PAR BIBLIOTHÈQUE
       ===================================================== */

    public long totalPretsParBibliotheque(Long bibId) {
        return pretRepository.countPretsActifsBibliotheque(bibId);
    }

  

    public long totalStockParBibliotheque(Long bibId) {
        return stockRepository.stockTotalParBibliotheque(bibId);
    }

    public double tauxRotationParBibliotheque(Long bibId) {

        long total = stockRepository.stockTotalParBibliotheque(bibId);
        long emprunte = stockRepository.stockEmprunteParBibliotheque(bibId);

        if (total == 0) {
            return 0.0;
        }

        return (double) emprunte * 100 / total;
    }

    /* =====================================================
       📊 GRAPHIQUES
       ===================================================== */

    public Map<String, Long> pretsParCategorieParBibliotheque(Long bibId) {

        Map<String, Long> result = new HashMap<>();

        List<Object[]> data =
                pretRepository.countPretsParCategorieBibliotheque(bibId);

        for (Object[] row : data) {
            result.put(String.valueOf(row[0]), (Long) row[1]);
        }

        return result;
    }

    public Map<String, Long> pretsParStatutParBibliotheque(Long bibId) {

        Map<String, Long> result = new HashMap<>();

        List<Object[]> data =
                pretRepository.countPretsParStatutBibliotheque(bibId);

        for (Object[] row : data) {
            result.put(String.valueOf(row[0]), (Long) row[1]);
        }

        return result;
    }

    /* =====================================================
       🧾 RAPPORT
       ===================================================== */

    public Rapport enregistrerRapport(
            TypeRapport type,
            Utilisateur admin,
            String contenuJson,
            String cheminExport) {

        Rapport rapport = Rapport.builder()
                .type(type)
                .generePar(admin)
                .dateGeneration(LocalDateTime.now())
                .contenuJson(contenuJson)
                .build();

        return rapportRepository.save(rapport);
    }
}
