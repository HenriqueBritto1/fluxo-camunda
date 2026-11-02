package com.br.fluxo.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TempoFeriasEnumTest {

    @ParameterizedTest
    @CsvSource({"SETE_DIAS, 7", "QUINZE_DIAS, 15", "TRINTA_DIAS, 30"})
    void deveRetornarQuantidadeDeDias(TempoFeriasEnum tempoFerias, int diasEsperados) {
        assertThat(tempoFerias.getValor()).isEqualTo(diasEsperados);
    }
}
