package com.bibliotheque.gestion_bibliotheque.metier;

import com.bibliotheque.gestion_bibliotheque.dao.UtilisateurRepository;
import com.bibliotheque.gestion_bibliotheque.entities.user.Role;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Utilisateur getByEmail(String email) {
    return utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
}
    // ================= LECTEUR =================
    public void registerLecteur(Utilisateur utilisateur) {

        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        utilisateur.setRole(Role.LECTEUR);
        utilisateur.setActif(true);

        utilisateurRepository.save(utilisateur);
    }

    // ================= AJOUT ADMIN =================
  public void creerAdministrateur(Utilisateur admin) {

    if (utilisateurRepository.existsByEmail(admin.getEmail())) {
        throw new IllegalArgumentException("Email déjà utilisé");
    }

    if (admin.getBibliotheque() == null) {
        throw new IllegalArgumentException("Bibliothèque obligatoire");
    }

    admin.setRole(Role.ADMIN);
    admin.setActif(true);
    admin.setMotDePasse(passwordEncoder.encode(admin.getMotDePasse()));

    utilisateurRepository.save(admin);
}

    // ================= LISTE ADMINS =================
     public Page<Utilisateur> getAdminsPaged(Pageable pageable) {
        return utilisateurRepository.findByRole(Role.ADMIN, pageable);
    }

    // ================= GET BY ID =================
    public Utilisateur getById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrateur introuvable"));
    }

    // ================= UPDATE ADMIN (CORRIGÉ) =================
    public void updateAdmin(Utilisateur adminForm) {

    Utilisateur admin = utilisateurRepository.findById(adminForm.getId())
            .orElseThrow(() -> new RuntimeException("Admin introuvable"));

    utilisateurRepository.findByEmail(adminForm.getEmail())
            .ifPresent(existing -> {
                if (!existing.getId().equals(admin.getId())) {
                    throw new IllegalArgumentException("Email déjà utilisé");
                }
            });

    admin.setNom(adminForm.getNom());
    admin.setPrenom(adminForm.getPrenom());
    admin.setEmail(adminForm.getEmail());
    admin.setActif(adminForm.isActif());
    admin.setRole(Role.ADMIN);

    // 🆕 BIBLIOTHÈQUE
    admin.setBibliotheque(adminForm.getBibliotheque());

    utilisateurRepository.save(admin);
}

    // ================= DELETE ADMIN =================
    public void deleteAdmin(Long id) {

        Utilisateur admin = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin introuvable"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Suppression non autorisée");
        }

        utilisateurRepository.delete(admin);
    }


    public Utilisateur creerBibliothecaire(Utilisateur bibliothecaire, Utilisateur adminConnecte) {
        bibliothecaire.setRole(Role.BIBLIOTHECAIRE);
        bibliothecaire.setBibliotheque(adminConnecte.getBibliotheque());
        bibliothecaire.setMotDePasse(passwordEncoder.encode(bibliothecaire.getMotDePasse()));

        return utilisateurRepository.save(bibliothecaire);
    }

    public Page<Utilisateur> getBibliothecairesPaged(
        Long bibliothequeId,
        Pageable pageable
) {
    return utilisateurRepository
            .findByBibliothequeId(bibliothequeId, pageable);
}

// ======================= UPDATE BIBLIOTHECAIRE =======================
public void updateBibliothecaire(Utilisateur form, Utilisateur adminConnecte) {

    Utilisateur biblio = utilisateurRepository.findById(form.getId())
            .orElseThrow(() -> new RuntimeException("Bibliothécaire introuvable"));

    if (!biblio.getBibliotheque().getId().equals(adminConnecte.getBibliotheque().getId())) {
        throw new RuntimeException("Accès interdit");
    }

    biblio.setNom(form.getNom());
    biblio.setPrenom(form.getPrenom());
    biblio.setEmail(form.getEmail());
    biblio.setActif(form.isActif());

    utilisateurRepository.save(biblio);
}

// ======================= DESACTIVATION =======================
public void desactiverBibliothecaire(Long id, Utilisateur adminConnecte) {

    Utilisateur biblio = utilisateurRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Bibliothécaire introuvable"));

    if (!biblio.getBibliotheque().getId().equals(adminConnecte.getBibliotheque().getId())) {
        throw new RuntimeException("Accès interdit");
    }

    biblio.setActif(false);

    utilisateurRepository.save(biblio);
}

}
