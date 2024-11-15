package com.example.To_Do.Liste;

import com.example.To_Do.Liste.model.Person;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final Person person;

    public CustomUserDetails(Person person) {
        this.person = person;
    }

    public Long getPersonid() {
        return person.getPersonid(); // Gibt die personid zurück
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // falls wir Rollen brauchen
    }

    @Override
    public String getPassword() {
        return person.getPasswort();
    }

    @Override
    public String getUsername() {
        return person.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Add getPerson() to allow access to the Person object
    public Person getPerson() {
        return person;
    }
}