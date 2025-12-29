package com.bibliotheque.gestion_bibliotheque.metier;

import com.bibliotheque.gestion_bibliotheque.dao.BibliothequeRepository;
import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.dao.StockBibliothequeRepository;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BibliothequeService {

    private final BibliothequeRepository bibliothequeRepository;
    private final UtilisateurService utilisateurService;
    private final PretRepository pretRepository;
    private final StockBibliothequeRepository stockRepository;

    public BibliothequeService(
            BibliothequeRepository bibliothequeRepository,
            UtilisateurService utilisateurService,
            PretRepository pretRepository,
            StockBibliothequeRepository stockRepository) {

        this.bibliothequeRepository = bibliothequeRepository;
        this.utilisateurService = utilisateurService;
        this.pretRepository = pretRepository;
        this.stockRepository = stockRepository;
    }

    // ==================================================
    // CRUD EXISTANT (INCHANGÉ)
    // ==================================================

    public Bibliotheque creerBibliotheque(Bibliotheque bibliotheque) {
        if (bibliothequeRepository.existsByCode(bibliotheque.getCode())) {
            throw new IllegalArgumentException("Le code de la bibliothèque existe déjà");
        }
        bibliotheque.setActif(true);
        return bibliothequeRepository.save(bibliotheque);
    }

    public Page<Bibliotheque> getAllPaged(Pageable pageable) {
        return bibliothequeRepository.findAll(pageable);
    }

    public List<Bibliotheque> getAll() {
        return bibliothequeRepository.findAll();
    }


    public List<Bibliotheque> listAll() {
        return bibliothequeRepository.findAll();
    }

    public Page<Bibliotheque> search(String keyword, String statut, Pageable pageable) {

    if (statut != null && statut.equals("active"))
        return bibliothequeRepository.search(keyword, true, pageable);

    if (statut != null && statut.equals("inactive"))
        return bibliothequeRepository.search(keyword, false, pageable);

    // sans filtre statut
    return bibliothequeRepository.search(keyword, null, pageable);
}
    // ================= READ BY ID =================

    public Bibliotheque getById(Long id) {
        return bibliothequeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bibliothèque introuvable"));
    }

    public void updateBibliotheque(Bibliotheque form) {
        Bibliotheque biblio = getById(form.getId());
        biblio.setNom(form.getNom());
        biblio.setCode(form.getCode());
        biblio.setTelephone(form.getTelephone());
        biblio.setAdresse(form.getAdresse());
        bibliothequeRepository.save(biblio);
    }

    public void desactiverBibliotheque(Long id) {
        Bibliotheque biblio = getById(id);
        biblio.setActif(false);
        bibliothequeRepository.save(biblio);
        utilisateurService.desactiverUtilisateursDeBibliotheque(biblio);
    }

    public void activerBibliotheque(Long id) {
        Bibliotheque biblio = getById(id);
        biblio.setActif(true);
        bibliothequeRepository.save(biblio);
        utilisateurService.activerUtilisateursDeBibliotheque(biblio);
    }

    // ==================================================
    // 📊 STATISTIQUES POUR DASHBOARD ADMIN
    // ==================================================

    // 🏛 Nombre de bibliothèques actives
    public long nombreBibliothequesActives() {
        return bibliothequeRepository.countByActifTrue();
    }

    // 🏛 Nombre de bibliothèques inactives
    public long nombreBibliothequesInactives() {
        return bibliothequeRepository.countByActifFalse();
    }

    // 📊 Nombre de prêts par bibliothèque
    public Map<String, Long> nombrePretsParBibliotheque() {

        Map<String, Long> result = new HashMap<>();

        pretRepository.countPretsParBibliotheque()
                .forEach(e -> {
                    String nomBibliotheque = (String) e[0];
                    Long nbPrets = (Long) e[1];
                    result.put(nomBibliotheque, nbPrets);
                });

        return result;
    }

    // 📦 Stock total par bibliothèque
    public Map<String, Long> stockTotalParBibliotheque() {

        Map<String, Long> result = new HashMap<>();

        stockRepository.tauxRotationParBibliotheque()
                .forEach(e -> {
                    String nomBibliotheque = (String) e[0];
                    Long quantiteTotale = (Long) e[2];
                    result.put(nomBibliotheque, quantiteTotale);
                });

        return result;
    }

    // 🔄 Taux de rotation du stock par bibliothèque
    public Map<String, Double> tauxRotationStockParBibliotheque() {

        Map<String, Double> result = new HashMap<>();

        stockRepository.tauxRotationParBibliotheque()
                .forEach(e -> {
                    String nomBibliotheque = (String) e[0];
                    Long emprunte = (Long) e[1];
                    Long total = (Long) e[2];

                    double taux = (total == null || total == 0)
                            ? 0
                            : (double) emprunte / total * 100;

                    result.put(nomBibliotheque, taux);
                });

        return result;
    }
    
 

}
