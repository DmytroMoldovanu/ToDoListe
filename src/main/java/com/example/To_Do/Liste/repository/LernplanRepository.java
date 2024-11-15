package com.example.To_Do.Liste.repository;

import com.example.To_Do.Liste.model.Lernplan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;  // Import List from java.util

@Repository
public interface LernplanRepository extends JpaRepository<Lernplan, Long> {

    List<Lernplan> findByPersonid(Long personId);  // Query method to find Lernplans by personId
}


