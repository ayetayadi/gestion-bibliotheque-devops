package com.bibliotheque.gestion_bibliotheque.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/lecteur")
@PreAuthorize("hasRole('LECTEUR')")
public class LecteurController {

    @GetMapping("/mes-prets")
    public String mesPrets() {
        return "lecteur/mes-prets";
    }

    @GetMapping("/reservations")
    public String reservations() {
        return "lecteur/reservations";
    }
}
