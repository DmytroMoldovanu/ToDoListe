package com.example.To_Do.Liste.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long personid;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwort;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "personen")
    private List<Projekt> projekte;

}
