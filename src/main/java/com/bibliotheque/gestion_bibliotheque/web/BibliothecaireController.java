package com.bibliotheque.gestion_bibliotheque.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/bibliothecaire")
@PreAuthorize("hasRole('BIBLIOTHECAIRE')")
public class BibliothecaireController {

    @GetMapping("/prets")
    public String prets() {
        return "bibliothecaire/prets";
    }

    @GetMapping("/ressources")
    public String ressources() {
        return "bibliothecaire/ressources";
    }
}
