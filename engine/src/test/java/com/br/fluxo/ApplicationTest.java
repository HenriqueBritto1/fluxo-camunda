package com.br.fluxo;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class ApplicationTest {

    @Test
    void deveIniciarAplicacaoSpringComArgumentosRecebidos() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            Application.main("--server.port=9090");

            springApplication.verify(() -> SpringApplication.run(Application.class, "--server.port=9090"));
        }
    }
}
