
package com.example.To_Do.Liste.controller;

import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.model.Todo;
import com.example.To_Do.Liste.model.Lernplan;
import com.example.To_Do.Liste.model.Projekt;
import com.example.To_Do.Liste.repository.PersonRepository;
import com.example.To_Do.Liste.repository.uebersichtRepository;
import com.example.To_Do.Liste.repository.LernplanRepository;
import com.example.To_Do.Liste.repository.ProjektRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class UebersichtController {

    private final uebersichtRepository uebersichtRepository;
    private final PersonRepository personRepository;
    private final LernplanRepository lernplanRepository;
    private final ProjektRepository projektRepository;

    @Autowired
    public UebersichtController(uebersichtRepository uebersichtRepository,
                                PersonRepository personRepository,
                                LernplanRepository lernplanRepository,
                                ProjektRepository projektRepository) {
        this.uebersichtRepository = uebersichtRepository;
        this.personRepository = personRepository;
        this.lernplanRepository = lernplanRepository;
        this.projektRepository = projektRepository;
    }

    // Zeigt das Formular zum Erstellen eines Todos
    @GetMapping("/todos/new")
    public String showCreateTodoForm(Model model) {
        model.addAttribute("todo", new Todo());

        // Lernpläne und Projekte laden (oder leere Listen, falls keine vorhanden)
        model.addAttribute("lernplaene", lernplanRepository.findAll().isEmpty() ? new ArrayList<>() : lernplanRepository.findAll());
        model.addAttribute("projekte", projektRepository.findAll().isEmpty() ? new ArrayList<>() : projektRepository.findAll());

        return "createTodo";
    }

    // Speichert ein neues Todo
    @PostMapping("/todos")
    public String createTodo(@ModelAttribute Todo todo, Authentication authentication) {
        String username = authentication.getName();
        Person user = personRepository.findByUsernameIgnoreCase(username);

        if (user == null) {
            throw new UsernameNotFoundException("Benutzer nicht gefunden: " + username);
        }

        todo.setPersonid(user.getPersonid()); // Setze den Benutzer für das Todo

        // Optionales Setzen von Lernplan und Projekt, wenn diese vorhanden sind
        if (todo.getLehrplanId() == null && todo.getProjektId() == null) {
            todo.setLehrplanId(null);
            todo.setProjektId(null);
        }

        // Speichern des neuen Startdatums und anderer Felder
        uebersichtRepository.save(todo);

        return "redirect:/uebersicht";
    }

    // Zeigt ein Todo im Detail an
    @GetMapping("/todos/{id}")
    public String viewTodo(@PathVariable Long id, Model model) {
        Todo todo = uebersichtRepository.findByTodoId(id);

        if (todo == null) {
            throw new IllegalArgumentException("Todo mit der ID " + id + " wurde nicht gefunden.");
        }

        // Lernplan-Titel und Projekt-Titel aus den Maps holen
        Map<Long, String> lernplanTitelMap = lernplanRepository.findAll().stream()
                .collect(Collectors.toMap(Lernplan::getLernplanId, Lernplan::getTitel));

        Map<Long, String> projektTitelMap = projektRepository.findAll().stream()
                .collect(Collectors.toMap(Projekt::getProjektId, Projekt::getTitel)); // Map für Projekte

        // Informationen dem Model hinzufügen
        model.addAttribute("todo", todo);
        model.addAttribute("lernplanTitelMap", lernplanTitelMap);
        model.addAttribute("projektTitelMap", projektTitelMap);

        return "viewTodo";  // Die Seite zum Anzeigen der Todo-Details
    }

    // Zeigt das Formular zum Bearbeiten eines Todos
    @GetMapping("/todos/{id}/edit")
    public String showEditTodoForm(@PathVariable Long id, Model model) {
        Todo todo = uebersichtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ungültige Todo-ID: " + id));

        model.addAttribute("todo", todo);
        model.addAttribute("lernplaene", lernplanRepository.findAll().isEmpty() ? new ArrayList<>() : lernplanRepository.findAll());
        model.addAttribute("projekte", projektRepository.findAll().isEmpty() ? new ArrayList<>() : projektRepository.findAll());

        return "editTodo";
    }

    // Aktualisiert ein Todo
    @PostMapping("/todos/{id}/edit")
    public String updateTodo(@PathVariable Long id, @ModelAttribute Todo todo) {
        Todo existingTodo = uebersichtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ungültige Todo-ID: " + id));

        // Optional: Update von Lernplan und Projekt
        if (todo.getLehrplanId() != null) {
            existingTodo.setLehrplanId(todo.getLehrplanId());
            existingTodo.setProjektId(null);  // Setze Projekt zurück, wenn Lernplan gesetzt wird
        } else if (todo.getProjektId() != null) {
            existingTodo.setProjektId(todo.getProjektId());
            existingTodo.setLehrplanId(null);  // Setze Lernplan zurück, wenn Projekt gesetzt wird
        }

        // Update der anderen Felder inklusive Startdatum
        existingTodo.setTitel(todo.getTitel());
        existingTodo.setBeschreibung(todo.getBeschreibung());
        existingTodo.setStart(todo.getStart()); // Startdatum aktualisieren
        existingTodo.setEnde(todo.getEnde());
        existingTodo.setStatus(todo.getStatus());

        // Speichern der Änderungen
        uebersichtRepository.save(existingTodo);

        return "redirect:/uebersicht";  // Redirect nach der Aktualisierung
    }

    // Löscht ein Todo
    @PostMapping("/todos/{id}/delete")
    public String deleteTodo(@PathVariable Long id) {
        uebersichtRepository.deleteById(id);
        return "redirect:/uebersicht";
    }

    // Zeigt eine Übersicht aller Todos des Benutzers
    @GetMapping("/uebersicht")
    public String showUebersicht(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(defaultValue = "titel") String sortBy,
                                 @RequestParam(defaultValue = "asc") String sortOrder,
                                 @RequestParam(required = false) String filter,
                                 Authentication authentication) {

        // Authentifizierung überprüfen
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Benutzer ist nicht authentifiziert.");
        }

        String username = authentication.getName();
        Person user = personRepository.findByUsernameIgnoreCase(username);

        if (user == null) {
            throw new UsernameNotFoundException("Benutzer nicht gefunden: " + username);
        }

        Long userId = user.getPersonid();
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Todo> userTodos;
        if (filter != null && !filter.isEmpty()) {
            userTodos = uebersichtRepository.findAllByPersonidAndTitelContainingIgnoreCase(userId, filter, pageable);
        } else {
            userTodos = uebersichtRepository.findAllByPersonid(userId, pageable);
        }

        // Lernplan-Titel und Projekt-Titel laden und in eine Map speichern
        Map<Long, String> lernplanTitelMap = lernplanRepository.findAll().stream()
                .collect(Collectors.toMap(Lernplan::getLernplanId, Lernplan::getTitel));

        Map<Long, String> projektTitelMap = projektRepository.findAll().stream()
                .collect(Collectors.toMap(Projekt::getProjektId, Projekt::getTitel)); // Map für Projekte

        model.addAttribute("todos", userTodos);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userTodos.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("lernplanTitelMap", lernplanTitelMap); // Lernplan-Titel-Map zur Vorlage hinzufügen
        model.addAttribute("projektTitelMap", projektTitelMap); // Projekt-Titel-Map zur Vorlage hinzufügen

        return "uebersicht";
    }
}