package com.bibliotheque.gestion_bibliotheque.metier;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.dao.RessourceRepository;
import com.bibliotheque.gestion_bibliotheque.entities.pret.StatutPret;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.Ressource;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.StatutRessource;
import com.bibliotheque.gestion_bibliotheque.entities.ressource.TypeCategorie;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final PretRepository pretRepo;
    private final RessourceRepository ressourceRepo;

    public List<Ressource> recommander(Utilisateur lecteur) {

        log.info("🔍 Calcul recommandation pour lecteur={} id={}",
                lecteur.getEmail(), lecteur.getId());

        // 1️⃣ Récupération des catégories préférées
        List<Object[]> stats = pretRepo.getTopCategoriesByLecteur(lecteur);

        if (stats.isEmpty()) {
            log.warn("⚠️ Aucun prêt → aucune recommandation possible.");
            return Collections.emptyList();
        }

        // Trouver la valeur max
        Long max = ((Number) stats.get(0)[1]).longValue();

        // Catégories ex-aequo
        Set<TypeCategorie> categoriesPref = stats.stream()
                .filter(s -> ((Number) s[1]).longValue() == max)
                .map(s -> (TypeCategorie) s[0])
                .collect(Collectors.toSet());

        log.info("🏆 Catégories préférées (ex aequo) = {}", categoriesPref);

        // 2️⃣ Ressources exclues (déjà prises ou en cours)
        Set<Long> dejaPris = pretRepo.findByLecteur(lecteur).stream()
                .filter(p ->
                        p.getStatut() == StatutPret.RESERVE ||
                        p.getStatut() == StatutPret.EMPRUNTE ||
                        p.getStatut() == StatutPret.EN_COURS)
                .map(p -> p.getRessource().getId())
                .collect(Collectors.toSet());

        log.info("⛔ Ressources déjà empruntées = {}", dejaPris);

        // 3️⃣ Recommandations filtrées
        List<Ressource> result = ressourceRepo.findAll().stream()
                .filter(r -> categoriesPref.contains(r.getCategorie()))
                .filter(r -> r.getStatut() == StatutRessource.DISPONIBLE)
                .filter(r -> !dejaPris.contains(r.getId()))
                .limit(10)
                .toList();

        log.info("🎁 Recommandations générées = {}", result);

        return result;
    }
}
