package com.br.fluxo.pedidoferias.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnviarNotificacaoServiceTest {

    private static final String REMETENTE = "rh@empresa.com";
    private static final String DESTINATARIO = "colaborador@empresa.com";

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> mensagemCaptor;

    @InjectMocks
    private EnviarNotificacaoService service;

    @BeforeEach
    void configurarRemetente() {
        ReflectionTestUtils.setField(service, "emailRemetente", REMETENTE);
    }

    @Test
    void deveEnviarEmailDeReprovacao() {
        service.enviarEmailReprovado(DESTINATARIO, "Período indisponível");

        verify(mailSender).send(mensagemCaptor.capture());
        SimpleMailMessage mensagem = mensagemCaptor.getValue();

        assertThat(mensagem.getTo()).containsExactly(DESTINATARIO);
        assertThat(mensagem.getFrom()).isEqualTo(REMETENTE);
        assertThat(mensagem.getSubject()).isEqualTo("Solicitação de férias negada");
        assertThat(mensagem.getText())
                .contains("sua solicitação de férias foi negada")
                .endsWith("Observação:\nPeríodo indisponível");
    }

    @Test
    void deveEnviarEmailDeAprovacao() {
        service.enviarEmailAprovado(DESTINATARIO, "15/03/2026", "Bom descanso");

        verify(mailSender).send(mensagemCaptor.capture());
        SimpleMailMessage mensagem = mensagemCaptor.getValue();

        assertThat(mensagem.getTo()).containsExactly(DESTINATARIO);
        assertThat(mensagem.getFrom()).isEqualTo(REMETENTE);
        assertThat(mensagem.getSubject()).isEqualTo("Férias aprovadas!");
        assertThat(mensagem.getText())
                .contains("sua solicitação de férias foi aprovada")
                .contains("Terá início dia: 15/03/2026")
                .endsWith("Observação:\nBom descanso");
    }
}
