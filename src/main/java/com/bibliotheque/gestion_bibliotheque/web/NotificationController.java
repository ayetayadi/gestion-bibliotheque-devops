package com.bibliotheque.gestion_bibliotheque.web;

import com.bibliotheque.gestion_bibliotheque.dao.NotificationRepository;
import com.bibliotheque.gestion_bibliotheque.entities.notification.Notification;
import com.bibliotheque.gestion_bibliotheque.entities.user.Utilisateur;
import com.bibliotheque.gestion_bibliotheque.metier.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UtilisateurService utilisateurService;

  @GetMapping("/api/notifications")
public List<Notification> getNotifications(Authentication auth) {
    Utilisateur user = utilisateurService.getByEmail(auth.getName());
    return notificationRepository.findActives(user);
}
}
