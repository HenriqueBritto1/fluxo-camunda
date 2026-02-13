package com.br.fluxo.pedidoferias.worker;




import com.br.fluxo.pedidoferias.service.EnviarNotificacaoService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class EnviarNotificacaoWorker {

    private final static Logger LOGGER = Logger.getLogger(EnviarNotificacaoWorker.class.getName());

    private final ExternalTaskClient client;

    @Autowired
    private EnviarNotificacaoService notificacaoService;

    public EnviarNotificacaoWorker(ExternalTaskClient client) {
        this.client = client;
    }

    @PostConstruct
    public void enviarNotificacao() {
        LOGGER.info("SUBSCRIBE EXECUTADO");

        client.subscribe("notifica-negado")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {
                    try {
                        LOGGER.info("Processo: " + externalTask.getProcessInstanceId());

                        String email = externalTask.getVariable("email");
                        LOGGER.info("Email: " + email);
                        notificacaoService.enviarEmail(email);

                        LOGGER.info("email enviado com sucesso!");
                        externalTaskService.complete(externalTask);
                    }catch (Exception ex){
                        Integer retries = externalTask.getRetries();
                        retries = (retries == null) ? 3 : retries - 1;

                        externalTaskService.handleFailure(
                                externalTask,
                                "Erro ao enviar notificação",
                                ex.getMessage(),
                                retries,
                                1000
                        );
                    }
                })
                .open();
    }

}
