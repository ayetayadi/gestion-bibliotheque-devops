package com.bibliotheque.gestion_bibliotheque.metier;

import com.bibliotheque.gestion_bibliotheque.dao.UtilisateurRepository;
import com.bibliotheque.gestion_bibliotheque.entities.user.Role;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // ================= UPDATE ADMIN =================
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

        // 🔐 rôle non modifiable
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

    // ================= GET BY EMAIL =================
    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // ================= UPDATE PROFIL + PHOTO =================
    public void updateProfile(
            String currentEmail,
            Utilisateur form,
            MultipartFile photoFile
    ) throws Exception {

        Utilisateur user = utilisateurRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 🔥 email unique
        utilisateurRepository.findByEmail(form.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(user.getId())) {
                        throw new IllegalArgumentException("Email déjà utilisé");
                    }
                });

        user.setNom(form.getNom());
        user.setPrenom(form.getPrenom());
        user.setEmail(form.getEmail());

        // 📸 PHOTO
        if (photoFile != null && !photoFile.isEmpty()) {

            String extension = photoFile.getOriginalFilename()
                    .substring(photoFile.getOriginalFilename().lastIndexOf("."));

            String fileName = "profile_" + user.getId() + extension;

            Path uploadDir = Paths.get("uploads/profiles");
            Files.createDirectories(uploadDir);

            Files.copy(
                    photoFile.getInputStream(),
                    uploadDir.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING
            );

            user.setPhoto(fileName);
        }

        utilisateurRepository.save(user);
    }
}
