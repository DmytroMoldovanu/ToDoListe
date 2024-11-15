package com.example.To_Do.Liste.dto;

import lombok.Setter;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Setter

public class TodoDto {

    private String titel;
    private LocalDate start;
    private LocalDate ende;
    private Long personId; // Person-Referenz
    private Integer status;

}