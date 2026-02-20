package com.br.fluxo.model;


import com.br.fluxo.common.TempoFeriasEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "ferias")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Ferias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String nome;

    @Column(name = "data_inicial")
    private LocalDate dataInicial;

    @Column(name = "data_final")
    private LocalDate dataFinal;

    @Column
    @Enumerated(EnumType.STRING)
    private TempoFeriasEnum quantidade;

    @Column
    private boolean aprovado;

    @Column
    private String observacao;

}