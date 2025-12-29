package com.bibliotheque.gestion_bibliotheque.scheduler;

import com.bibliotheque.gestion_bibliotheque.dao.PretRepository;
import com.bibliotheque.gestion_bibliotheque.entities.pret.Pret;
import com.bibliotheque.gestion_bibliotheque.metier.NotificationWSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
@Component
@RequiredArgsConstructor
@Slf4j
public class RetardScheduler {

    private final PretRepository pretRepository;
    private final NotificationWSService wsService;

    @Scheduled(fixedRate = 30_000)
    public void detecterRetards() {

        List<Pret> retards = pretRepository.findPretsEnRetard(LocalDateTime.now());

        for (Pret pret : retards) {

            String message = "⚠️ Vous avez dépassé la date de retour pour : "
                    + pret.getRessource().getTitre();

            wsService.notifyLecteur(
                    pret.getLecteur(),
                    pret.getRessource(),
                    message
            );
        }
    }
}
