package com.br.fluxo.delegate;

import com.br.fluxo.model.Ferias;
import com.br.fluxo.service.GravarFeriasService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class GravarFerias implements JavaDelegate {

    @Autowired
    private GravarFeriasService service;

    private final static Logger LOGGER = Logger.getLogger(GravarFerias.class.getName());

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        Ferias ferias = service.getVariaveisFerias(execution);

        LOGGER.info("Enviando Ferias");

        service.gravarFeriasDB(ferias);

        LOGGER.info("Ferias salvas com sucesso");
    }
}
