package com.bibliotheque.gestion_bibliotheque.web.superadmin;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.metier.BibliothequeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/super-admin/bibliotheques")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminBibliothequeController {

    private final BibliothequeService bibliothequeService;

    public SuperAdminBibliothequeController(BibliothequeService bibliothequeService) {
        this.bibliothequeService = bibliothequeService;
    }

    // ================= LISTE =================
    @GetMapping
    public String list(Model model) {
        model.addAttribute("bibliotheques", bibliothequeService.getAll());
        return "bibliotheque/list";
    }

    // ================= VIEW =================
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("bibliotheque", bibliothequeService.getById(id));
        return "bibliotheque/details";
    }

    // ================= ADD =================
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("bibliotheque", new Bibliotheque());
        return "bibliotheque/form";
    }

    @PostMapping("/add")
    public String create(
            @ModelAttribute Bibliotheque bibliotheque,
            RedirectAttributes redirectAttributes
    ) {
        bibliothequeService.creerBibliotheque(bibliotheque);
        redirectAttributes.addFlashAttribute(
                "success",
                "Bibliothèque créée avec succès"
        );
        return "redirect:/super-admin/bibliotheques";
    }

    // ================= EDIT =================
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("bibliotheque", bibliothequeService.getById(id));
        return "bibliotheque/form-edit";
    }

    @PostMapping("/edit")
    public String update(
            @ModelAttribute Bibliotheque bibliotheque,
            RedirectAttributes redirectAttributes
    ) {
        bibliothequeService.updateBibliotheque(bibliotheque);
        redirectAttributes.addFlashAttribute(
                "success",
                "Bibliothèque modifiée avec succès"
        );
        return "redirect:/super-admin/bibliotheques";
    }

    // ================= ACTIVER / DESACTIVER =================
    @GetMapping("/changer-statut/{id}")
    public String changerStatut(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        Bibliotheque biblio = bibliothequeService.getById(id);

        if (biblio.isActif()) {
            bibliothequeService.desactiverBibliotheque(id);
            redirectAttributes.addFlashAttribute(
                    "warning",
                    "Bibliothèque désactivée : tous les administrateurs et bibliothécaires associés ne peuvent plus se connecter."
            );
        } else {
            bibliothequeService.activerBibliotheque(id);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Bibliothèque réactivée : les utilisateurs associés peuvent se reconnecter."
            );
        }

        return "redirect:/super-admin/bibliotheques";
    }
}
