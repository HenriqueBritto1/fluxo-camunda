package com.br.fluxo.pedidoferias.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExternalTaskConfigTest {

    @Test
    void deveCriarClienteApontandoParaEngineLocalComTimeoutDeDezSegundos() {
        ExternalTaskClientBuilder construtor = mock(ExternalTaskClientBuilder.class, Answers.RETURNS_SELF);
        ExternalTaskClient clienteEsperado = mock(ExternalTaskClient.class);
        when(construtor.build()).thenReturn(clienteEsperado);

        try (MockedStatic<ExternalTaskClient> externalTaskClient = mockStatic(ExternalTaskClient.class)) {
            externalTaskClient.when(ExternalTaskClient::create).thenReturn(construtor);

            ExternalTaskClient cliente = new ExternalTaskConfig().externalTaskClient();

            assertThat(cliente).isSameAs(clienteEsperado);
        }

        verify(construtor).baseUrl("http://localhost:8080/engine-rest");
        verify(construtor).asyncResponseTimeout(10000);
    }
}
