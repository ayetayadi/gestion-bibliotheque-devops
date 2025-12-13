package com.bibliotheque.gestion_bibliotheque.config;

import com.bibliotheque.gestion_bibliotheque.dao.UtilisateurRepository;
import com.bibliotheque.gestion_bibliotheque.entities.user.Role;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initSuperAdmin(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            if (utilisateurRepository.findByEmail("superadmin@bibliotheque.com").isEmpty()) {

                Utilisateur admin = new Utilisateur();
                admin.setNom("Super");
                admin.setPrenom("Admin");
                admin.setEmail("superadmin@bibliotheque.com");
                admin.setMotDePasse(passwordEncoder.encode("admin123"));
                admin.setRole(Role.SUPER_ADMIN);
                admin.setActif(true);

                utilisateurRepository.save(admin);

                System.out.println("SUPER_ADMIN créé automatiquement");
            }
        };
    }
}
