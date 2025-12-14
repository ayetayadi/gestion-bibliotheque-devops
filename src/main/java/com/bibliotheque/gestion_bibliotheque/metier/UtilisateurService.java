package com.bibliotheque.gestion_bibliotheque.metier;

import com.bibliotheque.gestion_bibliotheque.dao.UtilisateurRepository;
import com.bibliotheque.gestion_bibliotheque.entities.user.Role;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
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

        if (utilisateurRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        admin.setRole(Role.ADMIN);
        admin.setActif(true);
        admin.setMotDePasse(passwordEncoder.encode(admin.getMotDePasse()));

        utilisateurRepository.save(admin);
    }

    // ================= LISTE ADMINS =================
    public List<Utilisateur> getAllAdmins() {
        return utilisateurRepository.findByRole(Role.ADMIN);
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

        // 🔥 VÉRIFICATION EMAIL UNIQUE (SAUF LUI-MÊME)
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
}
