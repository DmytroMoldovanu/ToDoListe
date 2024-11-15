package com.example.To_Do.Liste.repository;

import com.example.To_Do.Liste.model.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DashboardRepository extends JpaRepository<Todo, Long> {

    // Finde alle Todos für eine bestimmte Person mit Paginierung
    Page<Todo> findAllByPersonid(Long personId, Pageable pageable);

    // Finde alle Todos für eine bestimmte Person, bei denen der Titel einen bestimmten Text enthält, mit Paginierung
    Page<Todo> findAllByPersonidAndTitelContainingIgnoreCase(Long personId, String filter, Pageable pageable);

    // Alle Todos für eine bestimmte Person ohne Filter und Paginierung
    List<Todo> findAllByPersonid(Long personId);

    // Beispiel für eine benutzerdefinierte Methode im Repository
    @Query("SELECT t FROM Todo t WHERE t.personid = :personId AND FUNCTION('WEEK', t.start) = :weekOfYear")
    List<Todo> findAllByPersonidAndWeek(@Param("personId") Long personId, @Param("weekOfYear") int weekOfYear);

}
