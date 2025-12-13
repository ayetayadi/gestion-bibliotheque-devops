package com.bibliotheque.gestion_bibliotheque.metier;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bibliotheque.gestion_bibliotheque.dao.UtilisateurRepository;
import com.bibliotheque.gestion_bibliotheque.entities.user.Role;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                       PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerLecteur(Utilisateur utilisateur) {

        // 1️⃣ email unique
        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        // 2️⃣ hash mot de passe
        utilisateur.setMotDePasse(
            passwordEncoder.encode(utilisateur.getMotDePasse())
        );

        // 3️⃣ règles métier imposées par le sujet
        utilisateur.setRole(Role.LECTEUR);
        utilisateur.setActif(true);

        // 4️⃣ persistance
        utilisateurRepository.save(utilisateur);
    }
}
