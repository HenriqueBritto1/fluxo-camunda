package com.br.fluxo.pedidoferias.worker;

import com.br.fluxo.pedidoferias.service.EnviarNotificacaoService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Component
public class EnviarNotificacaoWorker {

    private static final Logger LOGGER = Logger.getLogger(EnviarNotificacaoWorker.class.getName());

    private static final String TOPICO_FERIAS_REPROVADAS = "notifica-negado";
    private static final String TOPICO_FERIAS_APROVADAS = "notifica-aprovado";
    
    private static final long TEMPO_BLOQUEIO_TAREFA_MS = 1000L;
    private static final int MAXIMO_TENTATIVAS = 3;
    private static final long TEMPO_ESPERA_TENTATIVA_MS = 1000L;
    
    private static final String VARIAVEL_EMAIL = "email";
    private static final String VARIAVEL_OBSERVACAO = "obs";
    private static final String VARIAVEL_DATA_FERIAS = "data_ferias";
    private static final String FORMATO_DATA_BR = "dd/MM/yyyy";
    private static final String MENSAGEM_ERRO_NOTIFICACAO = "Erro ao enviar notificação";

    private final ExternalTaskClient clienteCamunda;
    private final EnviarNotificacaoService notificacaoService;

    @Autowired
    public EnviarNotificacaoWorker(ExternalTaskClient clienteCamunda, EnviarNotificacaoService notificacaoService) {
        this.clienteCamunda = clienteCamunda;
        this.notificacaoService = notificacaoService;
    }

    @PostConstruct
    public void registrarWorkers() {
        inscreverTopicoFeriasReprovadas();
        inscreverTopicoFeriasAprovadas();
    }

    private void inscreverTopicoFeriasReprovadas() {
        LOGGER.info("Iniciando subscrição para o tópico de férias reprovadas: " + TOPICO_FERIAS_REPROVADAS);

        clienteCamunda.subscribe(TOPICO_FERIAS_REPROVADAS)
                .lockDuration(TEMPO_BLOQUEIO_TAREFA_MS)
                .handler(this::processarFeriasReprovadas)
                .open();
    }

    private void inscreverTopicoFeriasAprovadas() {
        LOGGER.info("Iniciando subscrição para o tópico de férias aprovadas: " + TOPICO_FERIAS_APROVADAS);
        
        clienteCamunda.subscribe(TOPICO_FERIAS_APROVADAS)
                .lockDuration(TEMPO_BLOQUEIO_TAREFA_MS)
                .handler(this::processarFeriasAprovadas)
                .open();
    }

    private void processarFeriasReprovadas(ExternalTask tarefaExterna, ExternalTaskService servicoTarefaExterna) {
        try {
            LOGGER.info("Processando férias reprovadas para a instância: " + tarefaExterna.getProcessInstanceId());

            String emailColaborador = obterVariavelString(tarefaExterna, VARIAVEL_EMAIL);
            String observacaoGestor = obterVariavelString(tarefaExterna, VARIAVEL_OBSERVACAO);

            notificacaoService.enviarEmailReprovado(emailColaborador, observacaoGestor);

            LOGGER.info("E-mail de reprovação enviado com sucesso!");
            servicoTarefaExterna.complete(tarefaExterna);
            
        } catch (Exception excecao) {
            tratarFalhaNaTarefa(tarefaExterna, servicoTarefaExterna, excecao);
        }
    }

    private void processarFeriasAprovadas(ExternalTask tarefaExterna, ExternalTaskService servicoTarefaExterna) {
        try {
            LOGGER.info("Processando férias aprovadas para a instância: " + tarefaExterna.getProcessInstanceId());

            String emailColaborador = obterVariavelString(tarefaExterna, VARIAVEL_EMAIL);
            String observacaoGestor = obterVariavelString(tarefaExterna, VARIAVEL_OBSERVACAO);
            String dataInicioFerias = obterVariavelString(tarefaExterna, VARIAVEL_DATA_FERIAS);
            
            String dataFormatada = formatarDataParaBrasileiro(dataInicioFerias);

            notificacaoService.enviarEmailAprovado(emailColaborador, dataFormatada, observacaoGestor);

            LOGGER.info("E-mail de aprovação enviado com sucesso!");
            servicoTarefaExterna.complete(tarefaExterna);
            
        } catch (Exception excecao) {
            tratarFalhaNaTarefa(tarefaExterna, servicoTarefaExterna, excecao);
        }
    }
    
    private String obterVariavelString(ExternalTask tarefaExterna, String nomeVariavel) {
        Object variavel = tarefaExterna.getVariable(nomeVariavel);
        return variavel != null ? variavel.toString() : "";
    }
    
    private String formatarDataParaBrasileiro(String dataPadraoIso) {
        if (dataPadraoIso == null || dataPadraoIso.isEmpty()) return "";
        return LocalDate.parse(dataPadraoIso).format(DateTimeFormatter.ofPattern(FORMATO_DATA_BR));
    }

    private void tratarFalhaNaTarefa(ExternalTask tarefaExterna, ExternalTaskService servicoTarefaExterna, Exception excecao) {
        LOGGER.severe("Erro ao processar tarefa: " + excecao.getMessage());
        
        Integer tentativasRestantes = tarefaExterna.getRetries();
        int novasTentativas = (tentativasRestantes == null) ? MAXIMO_TENTATIVAS : tentativasRestantes - 1;

        servicoTarefaExterna.handleFailure(
                tarefaExterna,
                MENSAGEM_ERRO_NOTIFICACAO,
                excecao.getMessage(),
                novasTentativas,
                TEMPO_ESPERA_TENTATIVA_MS
        );
    }
}
