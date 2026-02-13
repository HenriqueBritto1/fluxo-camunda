package com.br.fluxo.pedidoferias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EnviarNotificacaoService {

    @Value("${spring.mail.username}")
    private String username;

    private JavaMailSender mailSender;

    public EnviarNotificacaoService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmail(String to) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        String mensagem = "Olá, Prezado(a), sua solicitação de férias foi negada! Entre em contato com seu gestor.";

        mailMessage.setTo(to);
        mailMessage.setSubject("Solicitação de férias negada");
        mailMessage.setText(mensagem);
        mailMessage.setFrom(username);
        mailSender.send(mailMessage);
    }
}
