package com.example.To_Do.Liste.repository;

import com.example.To_Do.Liste.model.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface uebersichtRepository extends JpaRepository<Todo, Long> {

    Todo findByTodoId(Long todoid);

    // Methode für Pagination der Todos nach Person-ID
    Page<Todo> findAllByPersonid(Long personId, Pageable pageable);

    // Methode für Pagination der Todos nach Person-ID und Titel-Filtern
    Page<Todo> findAllByPersonidAndTitelContainingIgnoreCase(Long personId, String titel, Pageable pageable);
}
