package com.br.fluxo.pedidoferias;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class ApplicationWorkerTest {

    @Test
    void deveIniciarAplicacaoSpringComArgumentosRecebidos() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            ApplicationWorker.main(new String[]{"--server.port=9091"});

            springApplication.verify(() -> SpringApplication.run(ApplicationWorker.class, new String[]{"--server.port=9091"}));
        }
    }
}
