package com.example.To_Do.Liste.repository;

import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.model.Projekt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Person findByUsernameIgnoreCase(String username);

    Person findByUsername(String username);

    // Adjust this to use the correct relationship field
    @Query("SELECT p FROM Projekt p JOIN p.personen pe WHERE pe.personid = :personid")
    List<Projekt> findProjekteByPersonid(@Param("personid") Long personid);
}
