package com.bibliotheque.gestion_bibliotheque.metier;

import com.bibliotheque.gestion_bibliotheque.dao.BibliothequeRepository;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BibliothequeService {

    private final BibliothequeRepository bibliothequeRepository;
    private final UtilisateurService utilisateurService;

    public BibliothequeService(BibliothequeRepository bibliothequeRepository, UtilisateurService utilisateurService) {
        this.bibliothequeRepository = bibliothequeRepository;
        this.utilisateurService = utilisateurService;
    }

    // ================= LISTE =================
    public List<Bibliotheque> getAll() {
        return bibliothequeRepository.findAll();
    }

    // ================= GET =================
    public Bibliotheque getById(Long id) {
        return bibliothequeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bibliothèque introuvable"));
    }

    // ================= CREATE =================
    public void creerBibliotheque(Bibliotheque bibliotheque) {
        bibliotheque.setActif(true);
        bibliothequeRepository.save(bibliotheque);
    }

    // ================= UPDATE =================
    public void updateBibliotheque(Bibliotheque form) {
        Bibliotheque biblio = getById(form.getId());
        biblio.setNom(form.getNom());
        biblio.setCode(form.getCode());
        biblio.setAdresse(form.getAdresse());
        bibliothequeRepository.save(biblio);
    }

    // ================= DESACTIVER =================
    public void desactiverBibliotheque(Long id) {
        Bibliotheque biblio = getById(id);
        biblio.setActif(false);
        bibliothequeRepository.save(biblio);
        utilisateurService.desactiverUtilisateursDeBibliotheque(biblio);
    }

    // ================= ACTIVER =================
    public void activerBibliotheque(Long id) {

    Bibliotheque biblio = getById(id);
    biblio.setActif(true);
    bibliothequeRepository.save(biblio);

    utilisateurService.activerUtilisateursDeBibliotheque(biblio);
}
}
