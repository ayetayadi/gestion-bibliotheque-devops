package com.bibliotheque.gestion_bibliotheque.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    @GetMapping("/bibliotheques")
    public String bibliotheques() {
        return "super_admin/bibliotheques";
    }

    @GetMapping("/admins")
    public String admins() {
        return "super_admin/admins";
    }
}
