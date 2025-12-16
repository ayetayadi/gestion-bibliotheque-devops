package com.bibliotheque.gestion_bibliotheque.metier;

import com.bibliotheque.gestion_bibliotheque.dao.UtilisateurRepository;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.user.Role;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ================= UTIL =================
    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public Utilisateur getById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public Utilisateur getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return getByEmail(auth.getName());
    }

    // ================= LECTEUR =================
    public void registerLecteur(Utilisateur utilisateur) {

        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        utilisateur.setRole(Role.LECTEUR);
        utilisateur.setActif(true);
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));

        utilisateurRepository.save(utilisateur);
    }

    // ================= ADMIN =================
    public Page<Utilisateur> getAdminsPaged(Pageable pageable) {
        return utilisateurRepository.findByRole(Role.ADMIN, pageable);
    }

      public Page<Utilisateur> searchAdmins(
            String keyword,
            Long biblioId,
            Boolean statut,
            Pageable pageable
    ) {
        if (keyword != null && keyword.isBlank()) keyword = null;

        return utilisateurRepository.searchAdmins(
                keyword,
                biblioId,
                statut,
                pageable
        );
    }
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

    public void updateAdmin(Utilisateur form) {

        Utilisateur admin = getById(form.getId());

        utilisateurRepository.findByEmail(form.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(admin.getId())) {
                        throw new IllegalArgumentException("Email déjà utilisé");
                    }
                });

        admin.setNom(form.getNom());
        admin.setPrenom(form.getPrenom());
        admin.setEmail(form.getEmail());
        admin.setBibliotheque(form.getBibliotheque());
        admin.setActif(form.isActif());
        admin.setRole(Role.ADMIN);

        utilisateurRepository.save(admin);
    }

    public void desactiverAdmin(Long id) {
        Utilisateur admin = getById(id);
        admin.setActif(false);
        utilisateurRepository.save(admin);
    }

    public void activerAdmin(Long id) {
        Utilisateur admin = getById(id);

        if (admin.getBibliotheque() != null && !admin.getBibliotheque().isActif()) {
            throw new IllegalStateException(
                "Impossible d’activer cet administrateur : la bibliothèque est désactivée."
            );
        }

        admin.setActif(true);
        utilisateurRepository.save(admin);
    }

    // ================= BIBLIOTHÉCAIRE =================
    public Utilisateur creerBibliothecaire(Utilisateur biblio, Utilisateur admin) {

        // Vérif email
        if (utilisateurRepository.existsByEmail(biblio.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé.");
        }

        try {
            biblio.setRole(Role.BIBLIOTHECAIRE);
            biblio.setActif(true);
            biblio.setBibliotheque(admin.getBibliotheque());
            biblio.setMotDePasse(passwordEncoder.encode(biblio.getMotDePasse()));

            return utilisateurRepository.save(biblio);

        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Cet email est déjà utilisé.");
        }
    }

    public Page<Utilisateur> searchBibliothecaires(
        Long biblioId,
        String keyword,
        Boolean statut,
        Pageable pageable
) {
    if (keyword != null && keyword.isBlank()) keyword = null;

    return utilisateurRepository.searchBibliothecaires(
            biblioId,
            keyword,
            statut,
            pageable
    );
}

    public Utilisateur updateBibliothecaire(Utilisateur updated, Utilisateur admin) {

        Utilisateur original = utilisateurRepository.findById(updated.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Vérif même bibliothèque
        if (!original.getBibliotheque().getId().equals(admin.getBibliotheque().getId())) {
            throw new RuntimeException("Accès refusé");
        }

        // Vérif email unique
        if (!original.getEmail().equals(updated.getEmail())) {
            if (utilisateurRepository.findByEmail(updated.getEmail()).isPresent()) {
                throw new RuntimeException("Cet email est déjà utilisé.");
            }
        }

        original.setNom(updated.getNom());
        original.setPrenom(updated.getPrenom());
        original.setEmail(updated.getEmail());
        original.setActif(updated.isActif());

        try {
            return utilisateurRepository.save(original);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Cet email est déjà utilisé.");
        }
    }

    public Page<Utilisateur> getBibliothecairesPaged(Long bibliothequeId, Pageable pageable) {
        return utilisateurRepository.findByRoleAndBibliothequeId(
                Role.BIBLIOTHECAIRE,
                bibliothequeId,
                pageable
        );
    }

    public void desactiverBibliothecaire(Long id, Utilisateur adminConnecte) {

        Utilisateur biblio = getById(id);

        if (!biblio.getBibliotheque().getId()
                .equals(adminConnecte.getBibliotheque().getId())) {
            throw new RuntimeException("Accès interdit");
        }

        biblio.setActif(false);
        utilisateurRepository.save(biblio);
    }

    public void activerBibliothecaire(Long id, Utilisateur adminConnecte) {

        Utilisateur biblio = getById(id);

        if (!biblio.getBibliotheque().getId()
                .equals(adminConnecte.getBibliotheque().getId())) {
            throw new RuntimeException("Accès interdit");
        }

        if (!biblio.getBibliotheque().isActif()) {
            throw new IllegalStateException(
                "Impossible d’activer ce bibliothécaire : la bibliothèque est désactivée."
            );
        }

        biblio.setActif(true);
        utilisateurRepository.save(biblio);
    }

    // ================= BIBLIOTHÈQUES → ACTIVER / DÉSACTIVER UTILISATEURS =================
    public void desactiverUtilisateursDeBibliotheque(Bibliotheque bibliotheque) {
        utilisateurRepository.findByBibliothequeId(bibliotheque.getId())
                .forEach(u -> {
                    u.setActif(false);
                    utilisateurRepository.save(u);
                });
    }

    public void activerUtilisateursDeBibliotheque(Bibliotheque bibliotheque) {
        utilisateurRepository.findByBibliothequeId(bibliotheque.getId())
                .forEach(u -> {
                    u.setActif(true);
                    utilisateurRepository.save(u);
                });
    }

    // ================= PROFIL =================
    public void updateProfile(String currentEmail, Utilisateur form, MultipartFile photoFile)
            throws Exception {

        Utilisateur user = getByEmail(currentEmail);

        user.setNom(form.getNom());
        user.setPrenom(form.getPrenom());
        user.setEmail(form.getEmail());

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
