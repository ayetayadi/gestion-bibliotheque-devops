package com.bibliotheque.gestion_bibliotheque.config;

import com.bibliotheque.gestion_bibliotheque.dao.*;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Adresse;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.StockBibliotheque;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.*;
import com.bibliotheque.gestion_bibliotheque.entities.user.Role;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            UtilisateurRepository utilisateurRepository,
            BibliothequeRepository bibliothequeRepository,
            RessourceRepository ressourceRepository,
            StockBibliothequeRepository stockRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            /* ============================================================
             * 1️⃣ SUPER ADMIN
             * ============================================================ */
            if (utilisateurRepository.findByEmail("superadmin@bibliotheque.com").isEmpty()) {
                Utilisateur admin = new Utilisateur();
                admin.setNom("Super");
                admin.setPrenom("Admin");
                admin.setEmail("superadmin@bibliotheque.com");
                admin.setMotDePasse(passwordEncoder.encode("superadmin123"));
                admin.setRole(Role.SUPER_ADMIN);
                admin.setActif(true);
                utilisateurRepository.save(admin);
            }

            /* ============================================================
             * 2️⃣ BIBLIOTHÈQUE
             * ============================================================ */
            Bibliotheque biblio;

            if (bibliothequeRepository.count() == 0) {

                Adresse adresse = new Adresse();
                adresse.setRue("Rue des Fleurs");
                adresse.setVille("Paris");
                adresse.setCodePostal("75001");
                adresse.setPays("France");

                biblio = new Bibliotheque();
                biblio.setNom("Bibliothèque Centrale");
                biblio.setCode("BIB001");
                biblio.setTelephone("0123456789");
                biblio.setAdresse(adresse);

                bibliothequeRepository.save(biblio);

            } else {
                biblio = bibliothequeRepository.findAll().get(0);
            }

            /* ============================================================
             * 3️⃣ RESSOURCES + IMAGES (INSÉRÉES 1 SEULE FOIS)
             * ============================================================ */
            if (ressourceRepository.count() == 0) {

                Ressource r1 = new Ressource();
                r1.setTitre("Le Petit Prince");
                r1.setAuteur("Antoine de Saint-Exupéry");
                r1.setCategorie(TypeCategorie.LITTERATURE);
                r1.setTypeRessource(TypeRessource.LIVRE);
                r1.setDatePublication(LocalDate.of(1943, 4, 6));
                r1.setNbPages(96);
                r1.setCheminCouverture("https://covers.openlibrary.org/b/id/8231856-L.jpg");
                r1.setBibliotheque(biblio);
                r1.setStatut(StatutRessource.DISPONIBLE);

                Ressource r2 = new Ressource();
                r2.setTitre("Les Misérables");
                r2.setAuteur("Victor Hugo");
                r2.setCategorie(TypeCategorie.LITTERATURE);
                r2.setTypeRessource(TypeRessource.LIVRE);
                r2.setDatePublication(LocalDate.of(1862, 1, 1));
                r2.setNbPages(1400);
                r2.setCheminCouverture("https://covers.openlibrary.org/b/id/240727-L.jpg");
                r2.setBibliotheque(biblio);
                r2.setStatut(StatutRessource.DISPONIBLE);

                Ressource r3 = new Ressource();
                r3.setTitre("L'Étranger");
                r3.setAuteur("Albert Camus");
                r3.setCategorie(TypeCategorie.LITTERATURE);
                r3.setTypeRessource(TypeRessource.LIVRE);
                r3.setDatePublication(LocalDate.of(1942, 6, 10));
                r3.setNbPages(200);
                r3.setCheminCouverture("https://covers.openlibrary.org/b/id/11153207-L.jpg");
                r3.setBibliotheque(biblio);
                r3.setStatut(StatutRessource.DISPONIBLE);

                Ressource r4 = new Ressource();
                r4.setTitre("Physique Quantique - Introduction");
                r4.setAuteur("Yvan Meurice");
                r4.setCategorie(TypeCategorie.SCIENCES);
                r4.setTypeRessource(TypeRessource.LIVRE);
                r4.setDatePublication(LocalDate.of(2010, 3, 12));
                r4.setNbPages(500);
                r4.setCheminCouverture("https://covers.openlibrary.org/b/id/9251922-L.jpg");
                r4.setBibliotheque(biblio);
                r4.setStatut(StatutRessource.DISPONIBLE);

                Ressource r5 = new Ressource();
                r5.setTitre("Histoire de France pour les Nuls");
                r5.setAuteur("Jean-Joseph Julaud");
                r5.setCategorie(TypeCategorie.HISTOIRE);
                r5.setTypeRessource(TypeRessource.LIVRE);
                r5.setDatePublication(LocalDate.of(2008, 9, 21));
                r5.setNbPages(350);
                r5.setCheminCouverture("https://covers.openlibrary.org/b/id/8319256-L.jpg");
                r5.setBibliotheque(biblio);
                r5.setStatut(StatutRessource.DISPONIBLE);

                Ressource r6 = new Ressource();
                r6.setTitre("Grandes Époques de l’Art Moderne");
                r6.setAuteur("Pierre Souchaud");
                r6.setCategorie(TypeCategorie.ARTS);
                r6.setTypeRessource(TypeRessource.LIVRE);
                r6.setDatePublication(LocalDate.of(2015, 2, 18));
                r6.setNbPages(240);
                r6.setCheminCouverture("https://covers.openlibrary.org/b/id/8497982-L.jpg");
                r6.setBibliotheque(biblio);
                r6.setStatut(StatutRessource.DISPONIBLE);

                Ressource m1 = new Ressource();
                m1.setTitre("Documentaire : L’Univers");
                m1.setAuteur("National Geographic");
                m1.setCategorie(TypeCategorie.SCIENCES);
                m1.setTypeRessource(TypeRessource.MEDIA);
                m1.setDureeMinutes(52);
                m1.setCheminCouverture("https://covers.openlibrary.org/b/id/9873701-L.jpg");
                m1.setBibliotheque(biblio);
                m1.setStatut(StatutRessource.DISPONIBLE);

                Ressource m2 = new Ressource();
                m2.setTitre("Concert - Beethoven Symphonie n°5");
                m2.setAuteur("Orchestre de Paris");
                m2.setCategorie(TypeCategorie.ARTS);
                m2.setTypeRessource(TypeRessource.MEDIA);
                m2.setDureeMinutes(115);
                m2.setCheminCouverture("https://covers.openlibrary.org/b/id/554615-L.jpg");
                m2.setBibliotheque(biblio);
                m2.setStatut(StatutRessource.DISPONIBLE);

                Ressource m3 = new Ressource();
                m3.setTitre("Cours vidéo : Introduction à Java");
                m3.setAuteur("OpenClassrooms");
                m3.setCategorie(TypeCategorie.TECHNOLOGIE);
                m3.setTypeRessource(TypeRessource.MEDIA);
                m3.setDureeMinutes(180);
                m3.setCheminCouverture("https://covers.openlibrary.org/b/id/8235116-L.jpg");
                m3.setBibliotheque(biblio);
                m3.setStatut(StatutRessource.DISPONIBLE);

                Ressource d1 = new Ressource();
                d1.setTitre("Java - Guide complet PDF");
                d1.setAuteur("OC");
                d1.setCategorie(TypeCategorie.TECHNOLOGIE);
                d1.setTypeRessource(TypeRessource.DOCUMENT_NUMERIQUE);
                d1.setFormatFichier("PDF");
                d1.setTailleMo(12.5);
                d1.setCheminCouverture("https://covers.openlibrary.org/b/id/8235116-L.jpg");
                d1.setBibliotheque(biblio);
                d1.setStatut(StatutRessource.DISPONIBLE);

                Ressource d2 = new Ressource();
                d2.setTitre("Atlas des étoiles");
                d2.setAuteur("CNES");
                d2.setCategorie(TypeCategorie.SCIENCES);
                d2.setTypeRessource(TypeRessource.DOCUMENT_NUMERIQUE);
                d2.setFormatFichier("EPUB");
                d2.setTailleMo(22.0);
                d2.setCheminCouverture("https://covers.openlibrary.org/b/id/9873701-L.jpg");
                d2.setBibliotheque(biblio);
                d2.setStatut(StatutRessource.DISPONIBLE);

                Ressource d3 = new Ressource();
                d3.setTitre("Livre interactif : Apprendre en Jouant");
                d3.setAuteur("Hachette Jeunesse");
                d3.setCategorie(TypeCategorie.JEUNESSE);
                d3.setTypeRessource(TypeRessource.DOCUMENT_NUMERIQUE);
                d3.setFormatFichier("PDF");
                d3.setTailleMo(8.7);
                d3.setCheminCouverture("https://covers.openlibrary.org/b/id/240727-L.jpg");
                d3.setBibliotheque(biblio);
                d3.setStatut(StatutRessource.DISPONIBLE);

                List<Ressource> ressources = ressourceRepository.saveAll(
                        List.of(r1, r2, r3, r4, r5, r6, m1, m2, m3, d1, d2, d3)
                );

                for (Ressource r : ressources) {
                    StockBibliotheque stock = new StockBibliotheque();
                    stock.setBibliotheque(biblio);
                    stock.setRessource(r);
                    stock.setQuantiteTotale(5);
                    stock.setQuantiteDisponible(5);
                    stock.setQuantiteEmpruntee(0);
                    stock.setQuantiteReservee(0);
                    stockRepository.save(stock);
                }
            }

        }; 
    } 

} 
