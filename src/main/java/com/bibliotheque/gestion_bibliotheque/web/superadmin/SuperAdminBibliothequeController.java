package com.bibliotheque.gestion_bibliotheque.web.superadmin;

import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Adresse;
import com.bibliotheque.gestion_bibliotheque.entities.bibliotheque.Bibliotheque;
import com.bibliotheque.gestion_bibliotheque.metier.BibliothequeService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    
    // ============================= LISTE AVEC PAGINATION =============================
    @GetMapping
public String list(
        @RequestParam(defaultValue = "0") int page,
        Model model
) {

    if (page < 0) page = 0;

    System.out.println(">>> Requête page = " + page);

    Page<Bibliotheque> result =
            bibliothequeService.getAllPaged(PageRequest.of(page, 5));

    System.out.println(">>> Total bibliothèques = " + result.getTotalElements());
    System.out.println(">>> Total pages = " + result.getTotalPages());
    System.out.println(">>> Page courante = " + result.getNumber());
    System.out.println(">>> Nombre d'éléments dans cette page = " + result.getNumberOfElements());

    model.addAttribute("items", result.getContent());
    model.addAttribute("page", result);

    return "bibliotheque/list";
}



    // ============================= FORM AJOUT =============================
    @GetMapping("/add")
    public String createForm(Model model) {
        Bibliotheque b = new Bibliotheque();
        b.setAdresse(new Adresse()); // éviter NPE dans Thymeleaf

        model.addAttribute("bibliotheque", b);
        return "bibliotheque/form";
    }


    // ============================= SAVE NEW =============================
    @PostMapping
    public String save(
            @ModelAttribute Bibliotheque bibliotheque,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bibliothequeService.creerBibliotheque(bibliotheque);
            redirectAttributes.addFlashAttribute("success", "Bibliothèque créée avec succès");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/super-admin/bibliotheques";
    }


    // ============================= DETAILS =============================
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {

        model.addAttribute("bibliotheque", bibliothequeService.getById(id));

        return "bibliotheque/details";
    }


    // ============================= FORM EDIT =============================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {

        Bibliotheque b = bibliothequeService.getById(id);

        if (b.getAdresse() == null) {
            b.setAdresse(new Adresse());
        }

        model.addAttribute("bibliotheque", b);

        return "bibliotheque/form";
    }


    // ============================= UPDATE =============================
    @PostMapping("/edit")
    public String update(
            @ModelAttribute Bibliotheque bibliotheque,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bibliothequeService.update(bibliotheque);
            redirectAttributes.addFlashAttribute("success", "Bibliothèque modifiée avec succès");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/super-admin/bibliotheques";
    }


    // ============================= DELETE (SOFT) =============================
    @GetMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        bibliothequeService.delete(id);

        redirectAttributes.addFlashAttribute("success", "Bibliothèque désactivée avec succès");

        return "redirect:/super-admin/bibliotheques";
    }
}
