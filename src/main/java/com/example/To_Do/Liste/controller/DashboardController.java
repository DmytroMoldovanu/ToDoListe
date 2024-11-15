package com.example.To_Do.Liste.controller;

import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.model.Todo;
import com.example.To_Do.Liste.repository.DashboardRepository;
import com.example.To_Do.Liste.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardRepository dashboardRepository;
    private final PersonRepository personRepository;

    @Autowired
    public DashboardController(DashboardRepository dashboardRepository, PersonRepository personRepository) {
        this.dashboardRepository = dashboardRepository;
        this.personRepository = personRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "titel") String sortBy,
                                @RequestParam(defaultValue = "asc") String sortOrder,
                                @RequestParam(required = false) String filter,
                                Authentication authentication) {

        logger.info("showDashboard Methode wurde aufgerufen");

        // Sicherstellen, dass der Benutzer authentifiziert ist
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; // Weiterleitung zur Login-Seite, wenn nicht authentifiziert
        }

        // Benutzer laden
        String username = authentication.getName();
        Person user = personRepository.findByUsernameIgnoreCase(username);
        if (user == null) {
            throw new UsernameNotFoundException("Benutzer nicht gefunden: " + username);
        }

        // Berechne die Wochentage (Montag bis Sonntag) für die aktuelle Woche
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        LocalDate sunday = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 7);

        // Benutzer-Todos laden
        List<Todo> userTodos = dashboardRepository.findAllByPersonid(user.getPersonid());
        if (userTodos == null) {
            userTodos = new ArrayList<>();
        }

        // Entferne Todos ohne Start- oder Enddatum
        userTodos.removeIf(todo -> todo.getStart() == null || todo.getEnde() == null);

        // Map für die Wochentage initialisieren
        Map<String, List<Todo>> todosByWeekday = new HashMap<>();
        for (String day : Arrays.asList("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")) {
            todosByWeekday.put(day, new ArrayList<>()); // Initialisiert jeden Tag mit einer leeren Liste
        }

        // Todos den Wochentagen zuordnen
        for (Todo todo : userTodos) {
            LocalDate startDate = todo.getStart();
            LocalDate endDate = todo.getEnde();

            for (int i = 0; i < 7; i++) {
                LocalDate dayOfWeek = monday.plusDays(i);
                String dayKey = getDayOfWeekString(dayOfWeek);

                if ((dayOfWeek.isEqual(startDate) || dayOfWeek.isAfter(startDate)) &&
                        (dayOfWeek.isEqual(endDate) || dayOfWeek.isBefore(endDate)) &&
                        todo.getActiveDays() != null && todo.getActiveDays().contains(dayKey)) {
                    todosByWeekday.get(dayKey).add(todo);
                    logger.info("Todo '{}' added to day: {}", todo.getTitel(), dayKey);
                }
            }
        }

        // Füge die Map der Model-Attribute hinzu
        model.addAttribute("todosMap", todosByWeekday);
        return "dashboard";
    }

    private String getDayOfWeekString(LocalDate dayOfWeek) {
        String dayKey;
        switch (dayOfWeek.getDayOfWeek()) {
            case MONDAY:
                dayKey = "Mo";
                break;
            case TUESDAY:
                dayKey = "Di";
                break;
            case WEDNESDAY:
                dayKey = "Mi";
                break;
            case THURSDAY:
                dayKey = "Do";
                break;
            case FRIDAY:
                dayKey = "Fr";
                break;
            case SATURDAY:
                dayKey = "Sa";
                break;
            case SUNDAY:
                dayKey = "So";
                break;
            default:
                dayKey = "Mo"; // Default fallback
        }
        return dayKey;
    }
}
