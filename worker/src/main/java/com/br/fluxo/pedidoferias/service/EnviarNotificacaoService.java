package com.br.fluxo.pedidoferias.service;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;

@Service
public class EnviarNotificacaoService {

    private final ExternalTaskService externalTaskService;

    @Value("${spring.mail.username}")
    private String username;

    private JavaMailSender mailSender;

    public EnviarNotificacaoService(JavaMailSender mailSender, ExternalTaskService externalTaskService) {
        this.mailSender = mailSender;
        this.externalTaskService = externalTaskService;
    }

    public void enviarEmailReprovado(String to, String observacao) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        String mensagem = """
                Olá, Prezado(a),
                sua solicitação de férias foi negada! Entre em contato com seu gestor.
                
                Observação:
                """ + observacao;

        mailMessage.setTo(to);
        mailMessage.setSubject("Solicitação de férias negada");
        mailMessage.setText(mensagem);
        mailMessage.setFrom(username);
        mailSender.send(mailMessage);
    }

    public void enviarEmailAprovado(String to, String data, String observacao) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        String mensagem = """
                Olá, Prezado(a),
                sua solicitação de férias foi aprovada!
                Terá inicio dia:
                """ + data +
                """
                \n\nObservação:
                """ + observacao;
        mailMessage.setTo(to);
        mailMessage.setSubject("Férias aprovadas!");
        mailMessage.setText(mensagem);
        mailMessage.setFrom(username);
        mailSender.send(mailMessage);
    }
}
