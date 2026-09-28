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

    private final GravarFeriasService gravarFeriasService;
    private static final Logger LOGGER = Logger.getLogger(GravarFerias.class.getName());

    @Autowired
    public GravarFerias(GravarFeriasService gravarFeriasService) {
        this.gravarFeriasService = gravarFeriasService;
    }

    @Override
    public void execute(DelegateExecution execucaoDoProcesso) throws Exception {

        LOGGER.info("Iniciando a extração dos dados de Férias.");
        Ferias solicitacaoDeFerias = gravarFeriasService.extrairFeriasDaExecucao(execucaoDoProcesso);

        LOGGER.info("Enviando solicitação de Férias para o banco de dados.");
        gravarFeriasService.gravarFeriasDB(solicitacaoDeFerias);

        LOGGER.info("Férias salvas com sucesso no banco de dados.");
    }
}
