package com.example.To_Do.Liste.service;

import com.example.To_Do.Liste.model.Lernplan;
import com.example.To_Do.Liste.repository.LernplanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LernplanService {

    @Autowired
    private LernplanRepository lernplanRepository;  // Inject the LernplanRepository

    // Method to retrieve all Lernplans
    public List<Lernplan> getAllLernplans() {
        return lernplanRepository.findAll();  // Fetch all Lernplans from the database
    }

    // Method to retrieve a Lernplan by ID
    public Optional<Lernplan> getLernplanById(Long id) {
        return lernplanRepository.findById(id);  // Find a Lernplan by its ID
    }

    // Method to save a new or updated Lernplan
    public Lernplan saveLernplan(Lernplan lernplan) {
        return lernplanRepository.save(lernplan);  // Save the Lernplan to the database
    }

    // Method to delete a Lernplan by ID
    public void deleteLernplan(Long id) {
        lernplanRepository.deleteById(id);  // Delete the Lernplan from the database
    }
}
