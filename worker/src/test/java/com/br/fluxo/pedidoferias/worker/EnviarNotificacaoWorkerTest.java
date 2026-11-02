package com.br.fluxo.pedidoferias.worker;

import com.br.fluxo.pedidoferias.service.EnviarNotificacaoService;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnviarNotificacaoWorkerTest {

    private static final String EMAIL = "colaborador@empresa.com";
    private static final String OBSERVACAO = "Observação do gestor";
    private static final String ERRO = "falha no envio";
    private static final String MENSAGEM_ERRO = "Erro ao enviar notificação";
    private static final long TEMPO_BLOQUEIO_MS = 1000L;
    private static final long TEMPO_ESPERA_MS = 1000L;

    @Mock
    private ExternalTaskClient clienteCamunda;

    @Mock
    private EnviarNotificacaoService notificacaoService;

    @Mock(answer = Answers.RETURNS_SELF)
    private TopicSubscriptionBuilder inscricaoReprovadas;

    @Mock(answer = Answers.RETURNS_SELF)
    private TopicSubscriptionBuilder inscricaoAprovadas;

    @Mock
    private ExternalTask tarefa;

    @Mock
    private ExternalTaskService servicoTarefa;

    @InjectMocks
    private EnviarNotificacaoWorker worker;

    @BeforeEach
    void registrarWorkers() {
        when(clienteCamunda.subscribe("notifica-negado")).thenReturn(inscricaoReprovadas);
        when(clienteCamunda.subscribe("notifica-aprovado")).thenReturn(inscricaoAprovadas);

        worker.registrarWorkers();
    }

    @Test
    void deveAbrirInscricaoNosDoisTopicos() {
        verify(inscricaoReprovadas).lockDuration(TEMPO_BLOQUEIO_MS);
        verify(inscricaoReprovadas).open();
        verify(inscricaoAprovadas).lockDuration(TEMPO_BLOQUEIO_MS);
        verify(inscricaoAprovadas).open();
    }

    @Test
    void deveNotificarReprovacaoECompletarTarefa() {
        when(tarefa.getVariable("email")).thenReturn(EMAIL);
        when(tarefa.getVariable("obs")).thenReturn(OBSERVACAO);

        handlerDe(inscricaoReprovadas).execute(tarefa, servicoTarefa);

        verify(notificacaoService).enviarEmailReprovado(EMAIL, OBSERVACAO);
        verify(servicoTarefa).complete(tarefa);
    }

    @Test
    void deveNotificarReprovacaoComTextoVazioQuandoVariaveisEstiveremAusentes() {
        handlerDe(inscricaoReprovadas).execute(tarefa, servicoTarefa);

        verify(notificacaoService).enviarEmailReprovado("", "");
        verify(servicoTarefa).complete(tarefa);
    }

    @Test
    void deveNotificarAprovacaoComDataFormatadaECompletarTarefa() {
        when(tarefa.getVariable("email")).thenReturn(EMAIL);
        when(tarefa.getVariable("obs")).thenReturn(OBSERVACAO);
        when(tarefa.getVariable("data_ferias")).thenReturn("2026-03-15");

        handlerDe(inscricaoAprovadas).execute(tarefa, servicoTarefa);

        verify(notificacaoService).enviarEmailAprovado(EMAIL, "15/03/2026", OBSERVACAO);
        verify(servicoTarefa).complete(tarefa);
    }

    @Test
    void deveNotificarAprovacaoComDataVaziaQuandoVariavelEstiverAusente() {
        handlerDe(inscricaoAprovadas).execute(tarefa, servicoTarefa);

        verify(notificacaoService).enviarEmailAprovado("", "", "");
        verify(servicoTarefa).complete(tarefa);
    }

    @Test
    void deveDecrementarTentativasQuandoEnvioDeReprovacaoFalhar() {
        when(tarefa.getRetries()).thenReturn(2);
        doThrow(new IllegalStateException(ERRO))
                .when(notificacaoService).enviarEmailReprovado(anyString(), anyString());

        handlerDe(inscricaoReprovadas).execute(tarefa, servicoTarefa);

        verify(servicoTarefa).handleFailure(tarefa, MENSAGEM_ERRO, ERRO, 1, TEMPO_ESPERA_MS);
        verify(servicoTarefa, never()).complete(any(ExternalTask.class));
    }

    @Test
    void deveUsarMaximoDeTentativasNaPrimeiraFalhaDeAprovacao() {
        when(tarefa.getRetries()).thenReturn(null);
        doThrow(new IllegalStateException(ERRO))
                .when(notificacaoService).enviarEmailAprovado(anyString(), anyString(), anyString());

        handlerDe(inscricaoAprovadas).execute(tarefa, servicoTarefa);

        verify(servicoTarefa).handleFailure(tarefa, MENSAGEM_ERRO, ERRO, 3, TEMPO_ESPERA_MS);
        verify(servicoTarefa, never()).complete(any(ExternalTask.class));
    }

    @Test
    void deveRegistrarFalhaSemEnviarEmailQuandoDataForInvalida() {
        when(tarefa.getVariable("data_ferias")).thenReturn("15/03/2026");

        handlerDe(inscricaoAprovadas).execute(tarefa, servicoTarefa);

        verifyNoInteractions(notificacaoService);
        verify(servicoTarefa).handleFailure(any(ExternalTask.class), anyString(), anyString(), any(int.class), any(long.class));
    }

    private ExternalTaskHandler handlerDe(TopicSubscriptionBuilder inscricao) {
        ArgumentCaptor<ExternalTaskHandler> handlerCaptor = ArgumentCaptor.forClass(ExternalTaskHandler.class);
        verify(inscricao).handler(handlerCaptor.capture());
        return handlerCaptor.getValue();
    }
}
