package com.br.fluxo.pedidoferias.worker;


import com.br.fluxo.pedidoferias.model.Ferias;
import com.br.fluxo.pedidoferias.service.GravarFeriasService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class GravarFeriasWorker {

    private final static Logger LOGGER = Logger.getLogger(GravarFeriasWorker.class.getName());

    private final ExternalTaskClient client;

    @Autowired
    private GravarFeriasService feriasService;

    public GravarFeriasWorker(ExternalTaskClient client) {
        this.client = client;
    }

    @PostConstruct
    public void gravarFerias() {
        LOGGER.info("SUBSCRIBE EXECUTADO");

        client.subscribe("gravar-ferias")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {
                    Ferias ferias = feriasService.getVariaveisFerias(externalTask);

                    LOGGER.info("Enviando Ferias");

                    feriasService.gravarFeriasDB(ferias);

                    LOGGER.info("Ferias salvas com sucesso");
                    externalTaskService.complete(externalTask);
                })
                .open();
    }

}
