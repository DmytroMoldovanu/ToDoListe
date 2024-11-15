package com.example.To_Do.Liste.controller;

import com.example.To_Do.Liste.CustomUserDetails;
import com.example.To_Do.Liste.dto.TodoDto;
import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.model.Todo;
import com.example.To_Do.Liste.repository.TodoRepository;
import com.example.To_Do.Liste.repository.UserRepository;
import com.example.To_Do.Liste.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;


    @GetMapping
    public List<Map<String, Object>> getTodos(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long personId = userDetails.getPersonid();

        List<Todo> todos = todoRepository.findByPersonid(personId);

        List<Map<String, Object>> events = new ArrayList<>();
        for (Todo todo : todos) {
            Map<String, Object> event = new HashMap<>();
            event.put("id", todo.getTodoId());  // Die ID des Todos
            event.put("title", todo.getTitel());  // Der Titel des Todos (was auf dem Kalender angezeigt wird)
            event.put("start", todo.getStart().toString());  // Das Startdatum
            event.put("end", todo.getEnde() != null ? todo.getEnde().toString() : todo.getStart().toString());  // Das Enddatum (oder Startdatum falls Enddatum nicht gesetzt)
            events.add(event);
        }
        return events;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTodo(@RequestBody Map<String, String> requestData) {
        Todo todo = new Todo();
        todo.setTitel(requestData.get("titel"));
        todo.setStart(LocalDate.parse(requestData.get("start")));

        // Wenn das Enddatum vorhanden ist, setze es, sonst lasse es weg
        if (requestData.containsKey("ende") && requestData.get("ende") != null && !requestData.get("ende").isEmpty()) {
            todo.setEnde(LocalDate.parse(requestData.get("ende")));
        } else {
            todo.setEnde(null); // Optionales Enddatum nicht gesetzt
        }

        // Person laden und dem Todo zuweisen
        Long personId = Long.parseLong(requestData.get("person"));
        Optional<Person> person = userRepository.findById(personId);
        if (person.isPresent()) {
            todo.setPersonid(person.get().getPersonid());
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Person not found"));
        }

        // Status setzen (muss nicht null sein)
        if (requestData.containsKey("status") && requestData.get("status") != null) {
            todo.setStatus(requestData.get("status")); // Status direkt als String setzen
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Status not provided"));
        }

        todoRepository.save(todo);

        Map<String, Object> response = new HashMap<>();
        response.put("titel", todo.getTitel());
        response.put("start", todo.getStart().toString());

        // Sicherstellen, dass nur ein Enddatum hinzugefügt wird, wenn es nicht null ist
        if (todo.getEnde() != null) {
            response.put("ende", todo.getEnde().toString());
        } else {
            response.put("ende", null); // Oder einen Standardwert wie "kein Enddatum"
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        try {
            todoService.deleteTodo(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Todo not found");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodoDates(@PathVariable Long id, @RequestBody Map<String, String> requestData) {
        Optional<Todo> optionalTodo = todoRepository.findById(id);

        if (optionalTodo.isPresent()) {
            Todo todo = optionalTodo.get();

            // Setze das neue Startdatum
            if (requestData.containsKey("start")) {
                todo.setStart(LocalDate.parse(requestData.get("start")));
            }

            // Setze das neue Enddatum; prüfe auf "null"-Wert
            if (requestData.containsKey("ende") && requestData.get("ende") != null) {
                todo.setEnde(LocalDate.parse(requestData.get("ende")));
            } else {
                todo.setEnde(null);  // Setze Enddatum auf null
            }

            todoRepository.save(todo);
            return ResponseEntity.ok(Map.of("message", "Todo erfolgreich aktualisiert"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Todo nicht gefunden");
        }
    }
}
