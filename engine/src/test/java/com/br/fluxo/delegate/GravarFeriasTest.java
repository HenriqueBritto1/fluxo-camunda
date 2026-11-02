package com.br.fluxo.delegate;

import com.br.fluxo.model.Ferias;
import com.br.fluxo.service.GravarFeriasService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GravarFeriasTest {

    @Mock
    private GravarFeriasService gravarFeriasService;

    @Mock
    private DelegateExecution execucao;

    @InjectMocks
    private GravarFerias delegate;

    @Test
    void deveGravarFeriasExtraidasDaExecucao() throws Exception {
        Ferias solicitacao = new Ferias();
        when(gravarFeriasService.extrairFeriasDaExecucao(execucao)).thenReturn(solicitacao);

        delegate.execute(execucao);

        verify(gravarFeriasService).gravarFeriasDB(solicitacao);
    }

    @Test
    void naoDeveGravarQuandoExtracaoFalhar() {
        when(gravarFeriasService.extrairFeriasDaExecucao(execucao))
                .thenThrow(new IllegalArgumentException("dados inválidos"));

        assertThatThrownBy(() -> delegate.execute(execucao))
                .isInstanceOf(IllegalArgumentException.class);

        verify(gravarFeriasService, never()).gravarFeriasDB(any());
    }
}
