package com.example.To_Do.Liste.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "lernplan")
public class Lernplan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lernplanid")
    private Long lernplanId;

    @Column(name = "titel", nullable = false)
    private String titel;

    @Column(name = "start", nullable = false)
    private LocalDate start;

    @Column(name = "ende", nullable = false)
    private LocalDate ende;

    @Column(name = "personid", nullable = false)  // Foreign key reference to the Person entity
    private Long personid;

    @OneToMany(mappedBy = "lehrplanId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Todo> todos;

}

