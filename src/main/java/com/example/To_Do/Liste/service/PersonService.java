package com.example.To_Do.Liste.service;

import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonService {

    @Autowired
    private UserRepository userRepository;

    // Fetch a Person by username
    public Optional<Person> findByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    // Save a Person
    public void savePerson(Person person) {
        userRepository.save(person);
    }
}
