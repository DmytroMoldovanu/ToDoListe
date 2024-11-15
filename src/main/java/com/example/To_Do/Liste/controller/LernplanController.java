package com.example.To_Do.Liste.controller;

import com.example.To_Do.Liste.CustomUserDetails;
import com.example.To_Do.Liste.model.Lernplan;
import com.example.To_Do.Liste.repository.LernplanRepository;
import com.example.To_Do.Liste.service.LernplanService;
import com.example.To_Do.Liste.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/lernplan")
public class LernplanController {

    @Autowired
    private LernplanService lernplanService;

    @Autowired
    private LernplanRepository lernplanRepository;

    @Autowired
    private PersonService personService;

    // Display all Lernplans for the logged-in user
    @GetMapping
    public String showLernplaene(Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long personId = userDetails.getPerson().getPersonid();
        List<Lernplan> lernplaene = lernplanRepository.findByPersonid(personId);
        model.addAttribute("lernplaene", lernplaene);
        return "lernplan";
    }

    // Display the form to create a new Lernplan
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("lernplan", new Lernplan());
        return "lernplan-create";
    }

    // Handle the form submission to create a new Lernplan
    @PostMapping("/create")
    public String createLernplan(@RequestParam String titel, @RequestParam String start, @RequestParam String ende) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(ende);
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long personId = customUserDetails.getPerson().getPersonid();

        Lernplan newLernplan = new Lernplan();
        newLernplan.setTitel(titel);
        newLernplan.setStart(startDate);
        newLernplan.setEnde(endDate);
        newLernplan.setPersonid(personId);
        lernplanService.saveLernplan(newLernplan);
        return "redirect:/lernplan";
    }

    // Display the form to edit an existing Lernplan
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Lernplan lernplan = lernplanService.getLernplanById(id).orElse(null);
        if (lernplan != null) {
            model.addAttribute("lernplan", lernplan);
            return "lernplan-bearbeiten";
        }
        return "error";
    }

    // Handle the form submission to update an existing Lernplan
    @PostMapping("/edit/{id}")
    public String updateLernplan(@PathVariable Long id, @ModelAttribute Lernplan lernplan) {
        // Fetch existing Lernplan to ensure it exists
        Lernplan existingLernplan = lernplanService.getLernplanById(id).orElse(null);
        if (existingLernplan != null) {
            // Only update fields that need to be changed
            existingLernplan.setTitel(lernplan.getTitel());
            existingLernplan.setStart(lernplan.getStart());
            existingLernplan.setEnde(lernplan.getEnde());
            lernplanService.saveLernplan(existingLernplan);  // Save the updated Lernplan
        }
        return "redirect:/lernplan";
    }

    // Delete a Lernplan by ID
    @GetMapping("/delete/{id}")
    public String deleteLernplan(@PathVariable Long id) {
        lernplanService.deleteLernplan(id);
        return "redirect:/lernplan";
    }

    // Show details of a specific Lernplan, including its Todos
    @GetMapping("/{id}")
    public String showLernplanDetails(@PathVariable Long id, Model model) {
        Lernplan lernplan = lernplanService.getLernplanById(id).orElse(null);
        if (lernplan != null) {
            model.addAttribute("lernplan", lernplan);
            model.addAttribute("todos", lernplan.getTodos());
            return "lernplan-detail";
        }
        return "error";
    }
}