package com.bibliotheque.gestion_bibliotheque.metier;

import com.bibliotheque.gestion_bibliotheque.dao.BibliothequeRepository;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BibliothequeService {

    private final BibliothequeRepository bibliothequeRepository;

    // ================= CREATE =================
    public Bibliotheque creerBibliotheque(Bibliotheque bibliotheque) {

        if (bibliothequeRepository.existsByCode(bibliotheque.getCode())) {
            throw new IllegalArgumentException(
                "Le code de la bibliothèque existe déjà"
            );
        }

        bibliotheque.setActif(true);
        return bibliothequeRepository.save(bibliotheque);
    }

    // ================= READ PAGINÉ =================
    public Page<Bibliotheque> getAllPaged(Pageable pageable) {
        return bibliothequeRepository.findAll(pageable);
    }

    // ================= READ SIMPLE (utilisé par SuperAdmin) =================
    public List<Bibliotheque> getAll() {
        return bibliothequeRepository.findAll();
    }

    // Alias (si tu veux garder listAll)
    public List<Bibliotheque> listAll() {
        return bibliothequeRepository.findAll();
    }

    // ================= READ BY ID =================
    public Bibliotheque getById(Long id) {
        return bibliothequeRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Bibliothèque introuvable"));
    }

    // ================= UPDATE =================
    public Bibliotheque update(Bibliotheque updated) {

        Bibliotheque existing = getById(updated.getId());

        existing.setNom(updated.getNom());
        existing.setCode(updated.getCode());
        existing.setAdresse(updated.getAdresse());

        return bibliothequeRepository.save(existing);
    }

    // ================= DELETE (soft delete) =================
    public void delete(Long id) {
        Bibliotheque b = getById(id);
        b.setActif(false);
        bibliothequeRepository.save(b);
    }
}
