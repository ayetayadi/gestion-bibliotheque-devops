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

    // =====================================================
    // 📊 KPI GLOBAUX (ADMIN)
    // =====================================================

    public long totalPrets() {
        return pretRepository.count();
    }

    public long totalPretsActifs() {
        return pretRepository.countPretsActifs();
    }

    public long totalStock() {
        Long total = stockRepository.totalStock();
        return total != null ? total : 0;
    }

    public long totalStockEmprunte() {
        Long emprunte = stockRepository.totalStockEmprunte();
        return emprunte != null ? emprunte : 0;
    }

    // =====================================================
    // 📊 STATISTIQUES DASHBOARD
    // =====================================================

    /**
     * 📚 Nombre de prêts par catégorie
     */
    public Map<String, Long> pretsParCategorie() {

        Map<String, Long> result = new HashMap<>();

        List<Object[]> data = pretRepository.countPretsParCategorie();

        for (Object[] row : data) {
            String categorie = String.valueOf(row[0]);
            Long nb = (Long) row[1];
            result.put(categorie, nb);
        }

        return result;
    }

    /**
     * 🏛️ Nombre de prêts par bibliothèque
     */
    public Map<String, Long> pretsParBibliotheque() {

        Map<String, Long> result = new HashMap<>();

        List<Object[]> data = pretRepository.countPretsParBibliotheque();

        for (Object[] row : data) {
            String bibliotheque = String.valueOf(row[0]);
            Long nb = (Long) row[1];
            result.put(bibliotheque, nb);
        }

        return result;
    }

    /**
     * 👤 Activité des utilisateurs
     */
    public Map<String, Long> activiteUtilisateurs() {

        Map<String, Long> result = new HashMap<>();

        List<Object[]> data = pretRepository.countPretsParUtilisateur();

        for (Object[] row : data) {
            String email = String.valueOf(row[0]);
            Long nb = (Long) row[1];
            result.put(email, nb);
        }

        return result;
    }

    // =====================================================
    // 🔄 TAUX DE ROTATION DU STOCK
    // =====================================================

    /**
     * 📦 Taux de rotation global (%)
     */
    public double tauxRotationGlobal() {

        long total = totalStock();
        long emprunte = totalStockEmprunte();

        if (total == 0) {
            return 0.0;
        }

        return (double) emprunte * 100 / total;
    }

    /**
     * 📦 Taux de rotation par bibliothèque (%)
     */
    public Map<String, Double> tauxRotationParBibliotheque() {

        Map<String, Double> result = new HashMap<>();

        stockRepository.tauxRotationParBibliotheque().forEach(row -> {

            String bibliotheque = String.valueOf(row[0]);
            Long emprunte = (Long) row[1];
            Long total = (Long) row[2];

            double taux = (total == null || total == 0)
                    ? 0.0
                    : (double) emprunte * 100 / total;

            result.put(bibliotheque, taux);
        });

        return result;
    }

    // =====================================================
    // 🧾 HISTORIQUE DES RAPPORTS
    // =====================================================

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
                .cheminExport(cheminExport)
                .build();

        return rapportRepository.save(rapport);
    }
}
