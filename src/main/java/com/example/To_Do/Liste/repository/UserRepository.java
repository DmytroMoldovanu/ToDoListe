package com.example.To_Do.Liste.repository;

import com.example.To_Do.Liste.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Person, Long> {
    Person findByUsername(String benutzername);
}