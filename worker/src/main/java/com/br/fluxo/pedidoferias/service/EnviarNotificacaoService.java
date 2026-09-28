package com.br.fluxo.pedidoferias.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EnviarNotificacaoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailRemetente;

    public EnviarNotificacaoService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailReprovado(String emailDestinatario, String observacaoGestor) {
        String assunto = "Solicitação de férias negada";
        String mensagem = montarMensagemReprovacao(observacaoGestor);
        
        enviarEmail(emailDestinatario, assunto, mensagem);
    }

    public void enviarEmailAprovado(String emailDestinatario, String dataInicioFerias, String observacaoGestor) {
        String assunto = "Férias aprovadas!";
        String mensagem = montarMensagemAprovacao(dataInicioFerias, observacaoGestor);

        enviarEmail(emailDestinatario, assunto, mensagem);
    }

    private String montarMensagemReprovacao(String observacaoGestor) {
        return "Olá, Prezado(a),\n" +
               "sua solicitação de férias foi negada! Entre em contato com seu gestor.\n\n" +
               "Observação:\n" + observacaoGestor;
    }

    private String montarMensagemAprovacao(String dataInicioFerias, String observacaoGestor) {
        return "Olá, Prezado(a),\n" +
               "sua solicitação de férias foi aprovada!\n" +
               "Terá início dia: " + dataInicioFerias + "\n\n" +
               "Observação:\n" + observacaoGestor;
    }

    private void enviarEmail(String destinatario, String assunto, String textoMensagem) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(destinatario);
        mailMessage.setSubject(assunto);
        mailMessage.setText(textoMensagem);
        mailMessage.setFrom(emailRemetente);
        mailSender.send(mailMessage);
    }
}
