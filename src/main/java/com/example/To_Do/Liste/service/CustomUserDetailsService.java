package com.example.To_Do.Liste.service;

import com.example.To_Do.Liste.CustomUserDetails;
import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.repository.PersonRepository;
import com.example.To_Do.Liste.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private PersonRepository personRepository; // PersonRepository

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Person user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(user); // Hier wird CustomUserDetails verwendet
    }

    public Person getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }


    // Speichert den Benutzer
    public void saveUser(Person user) {
        userRepository.save(user);
    }

    // Methode zum Löschen eines Benutzers
    public void deleteUserById(Long id) {
        if (!personRepository.existsById(id)) {
            throw new UsernameNotFoundException("Benutzer nicht gefunden mit ID: " + id);
        }
        personRepository.deleteById(id); // Benutzer löschen
    }
}
