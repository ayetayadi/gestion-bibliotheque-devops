package com.bibliotheque.gestion_bibliotheque.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public String dashboard() {
        return "dashboard";
    }
}
