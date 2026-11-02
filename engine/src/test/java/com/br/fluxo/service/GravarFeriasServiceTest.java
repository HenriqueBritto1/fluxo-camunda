package com.br.fluxo.service;

import com.br.fluxo.common.TempoFeriasEnum;
import com.br.fluxo.model.Ferias;
import com.br.fluxo.repository.FeriasRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GravarFeriasServiceTest {

    @Mock
    private FeriasRepository feriasRepository;

    @Mock
    private DelegateExecution execucao;

    @InjectMocks
    private GravarFeriasService service;

    @Test
    void deveSalvarSolicitacaoNoRepositorio() {
        Ferias solicitacao = new Ferias();

        service.gravarFeriasDB(solicitacao);

        verify(feriasRepository).save(solicitacao);
    }

    @Test
    void deveExtrairTodosOsCamposDaExecucao() {
        when(execucao.getVariable("nome")).thenReturn("Maria");
        when(execucao.getVariable("data_ferias")).thenReturn("2026-01-10");
        when(execucao.getVariable("tempo_ferias")).thenReturn("SETE_DIAS");
        when(execucao.getVariable("aprovado")).thenReturn(true);
        when(execucao.getVariable("obs")).thenReturn("Boas férias");

        Ferias ferias = service.extrairFeriasDaExecucao(execucao);

        assertThat(ferias.getNome()).isEqualTo("Maria");
        assertThat(ferias.getDataInicial()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(ferias.getQuantidade()).isEqualTo(TempoFeriasEnum.SETE_DIAS);
        assertThat(ferias.isAprovado()).isTrue();
        assertThat(ferias.getObservacao()).isEqualTo("Boas férias");
    }

    @ParameterizedTest
    @CsvSource({
            "SETE_DIAS, 2026-01-16",
            "QUINZE_DIAS, 2026-01-24",
            "TRINTA_DIAS, 2026-02-08"
    })
    void deveCalcularDataFinalContandoODiaInicial(String tempoFerias, LocalDate dataFinalEsperada) {
        when(execucao.getVariable("data_ferias")).thenReturn("2026-01-10");
        when(execucao.getVariable("tempo_ferias")).thenReturn(tempoFerias);

        Ferias ferias = service.extrairFeriasDaExecucao(execucao);

        assertThat(ferias.getDataFinal()).isEqualTo(dataFinalEsperada);
    }

    @Test
    void deveConsiderarNaoAprovadoQuandoVariavelForFalsa() {
        when(execucao.getVariable("data_ferias")).thenReturn("2026-01-10");
        when(execucao.getVariable("tempo_ferias")).thenReturn("SETE_DIAS");
        when(execucao.getVariable("aprovado")).thenReturn(false);

        assertThat(service.extrairFeriasDaExecucao(execucao).isAprovado()).isFalse();
    }

    @Test
    void deveUsarValoresPadraoQuandoVariaveisOpcionaisEstiveremAusentes() {
        when(execucao.getVariable("data_ferias")).thenReturn("2026-01-10");
        when(execucao.getVariable("tempo_ferias")).thenReturn("SETE_DIAS");

        Ferias ferias = service.extrairFeriasDaExecucao(execucao);

        assertThat(ferias.getNome()).isEmpty();
        assertThat(ferias.getObservacao()).isEmpty();
        assertThat(ferias.isAprovado()).isFalse();
    }

    @Test
    void deveLancarExcecaoQuandoDataDeInicioEstiverAusente() {
        when(execucao.getVariable("tempo_ferias")).thenReturn("SETE_DIAS");

        assertThatThrownBy(() -> service.extrairFeriasDaExecucao(execucao))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void deveLancarExcecaoQuandoTempoDeFeriasForInvalido() {
        when(execucao.getVariable("data_ferias")).thenReturn("2026-01-10");
        when(execucao.getVariable("tempo_ferias")).thenReturn("DEZ_DIAS");

        assertThatThrownBy(() -> service.extrairFeriasDaExecucao(execucao))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
