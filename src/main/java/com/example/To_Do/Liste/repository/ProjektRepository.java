package com.example.To_Do.Liste.repository;

import com.example.To_Do.Liste.model.Projekt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjektRepository extends JpaRepository<Projekt, Long> {

    // Projekte basierend auf der OwnerID abfragen
    List<Projekt> findByOwnerid(Long ownerid);
}