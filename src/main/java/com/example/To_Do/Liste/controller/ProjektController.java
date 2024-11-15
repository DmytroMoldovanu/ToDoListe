package com.example.To_Do.Liste.controller;

import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.model.Projekt;
import com.example.To_Do.Liste.repository.PersonRepository;
import com.example.To_Do.Liste.repository.ProjektRepository;
import com.example.To_Do.Liste.CustomUserDetails;  // Importiere CustomUserDetails
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/projekt")
public class ProjektController {

    private final ProjektRepository projektRepository;
    private final PersonRepository personRepository;

    @Autowired
    public ProjektController(ProjektRepository projektRepository, PersonRepository personRepository) {
        this.projektRepository = projektRepository;
        this.personRepository = personRepository;
    }

    @GetMapping
    public String showProjekt(Model model) {
        // Extrahiere das Person-Objekt aus CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Person person = userDetails.getPerson();  // Hol dir das Person-Objekt von CustomUserDetails

        // Abfragen der Projekte des Besitzers und der Projekte, an denen die Person teilnimmt
        List<Projekt> ownedProjects = projektRepository.findByOwnerid(person.getPersonid());
        List<Projekt> participantProjects = personRepository.findProjekteByPersonid(person.getPersonid());

        if (participantProjects == null) {
            participantProjects = Collections.emptyList();
        }

        // Wenn die Listen null sind, werden sie zu leeren Listen
        model.addAttribute("ownedProjects", ownedProjects != null ? ownedProjects : Collections.emptyList());
        model.addAttribute("participantProjects", participantProjects != null ? participantProjects : Collections.emptyList());

        return "projekt";
    }

    @PostMapping("/create")
    public String createProject(
            @RequestParam("titel") String titel,
            @RequestParam("beschreibung") String beschreibung,
            Principal principal
    ) {
        // Hole den aktuell angemeldeten Benutzer basierend auf dem Principal
        Person currentUser = personRepository.findByUsername(principal.getName());
        // Prüfe, ob der Benutzer existiert, bevor fortgefahren wird
        if (currentUser == null) {
            // Handle error (z.B., weiterleiten oder Fehlermeldung anzeigen)
            return "redirect:/error";
        }

        // Erstelle ein neues Projekt und setze die Felder
        Projekt projekt = new Projekt();
        projekt.setTitel(titel);
        projekt.setBeschreibung(beschreibung);
        projekt.setOwnerid(currentUser.getPersonid()); // Setze die personid als Besitzer
        projekt.setTodos(new ArrayList<>()); // Initialisiert die To-do-Liste als leere Liste

        // Speichere das Projekt
        projektRepository.save(projekt);

        // Nach dem Erstellen des Projekts zur Projektseite zurückkehren
        return "redirect:/projekt";
    }

    @PostMapping("/delete")
    public String deleteProject(@RequestParam("projektid") Long projektid) {
        projektRepository.deleteById(projektid);
        return "redirect:/projekt";
    }

    @PostMapping("/addMember")
    public String addMember(@RequestParam Long projektid, @RequestParam String username, Model model) {
        try {
            // Projekt suchen
            Projekt projekt = projektRepository.findById(projektid)
                    .orElseThrow(() -> new IllegalArgumentException("Projekt nicht gefunden"));

            // Person anhand des Benutzernamens suchen
            Person person = personRepository.findByUsername(username);
            if (person == null) {
                throw new UsernameNotFoundException("Benutzer nicht gefunden");
            }

            // Nur den Benutzer hinzufügen, wenn er noch nicht Mitglied ist
            if (!projekt.getPersonen().contains(person)) {
                projekt.getPersonen().add(person);
                projektRepository.save(projekt);
            }
        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            // Fehler zur Anzeige an das Modell übergeben
            model.addAttribute("error", e.getMessage());
            return "projekt"; // Zurück zum Projekt-Template mit einer Fehlernachricht
        }

        return "redirect:/projekt"; // Erfolgreiche Weiterleitung zur Projektseite
    }

    @PostMapping("/removeMember")
    public String removeMember(@RequestParam Long projektid, @RequestParam String username, Model model) {
        try {
            // Projekt suchen
            Projekt projekt = projektRepository.findById(projektid)
                    .orElseThrow(() -> new IllegalArgumentException("Projekt nicht gefunden"));

            // Person anhand des Benutzernamens suchen
            Person person = personRepository.findByUsername(username);
            if (person == null) {
                throw new UsernameNotFoundException("Benutzer nicht gefunden");
            }

            // Nur den Benutzer entfernen, wenn er Mitglied ist
            if (projekt.getPersonen().contains(person)) {
                projekt.getPersonen().remove(person);
                projektRepository.save(projekt); // Speichern nach dem Entfernen
            } else {
                model.addAttribute("error", "Benutzer ist kein Mitglied des Projekts.");
            }

        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            // Fehler zur Anzeige an das Modell übergeben
            model.addAttribute("error", e.getMessage());
            return "projekt"; // Zurück zum Projekt-Template mit einer Fehlernachricht
        }

        return "redirect:/projekt"; // Erfolgreiche Weiterleitung zur Projektseite
    }
}