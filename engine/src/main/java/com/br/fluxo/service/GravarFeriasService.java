package com.br.fluxo.service;


import com.br.fluxo.common.TempoFeriasEnum;
import com.br.fluxo.model.Ferias;
import com.br.fluxo.repository.FeriasRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class GravarFeriasService {

    @Autowired
    private FeriasRepository feriasRepository;

    public void gravarFeriasDB(Ferias ferias) {
        feriasRepository.save(ferias);
    }

    public Ferias getVariaveisFerias(DelegateExecution execution) {

        String nome = execution.getVariable("nome").toString();

        String data = execution.getVariable("data_ferias").toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dataFormatadaInicial = LocalDate.parse(data, formatter);

        TempoFeriasEnum tempo = TempoFeriasEnum.valueOf((String) execution.getVariable("tempo_ferias"));

        LocalDate dataFormatadaFinal = dataFormatadaInicial.plusDays(tempo.getValor() - 1);

        Boolean aprovado = (Boolean) execution.getVariable("aprovado");

        String observacao = execution.getVariable("obs").toString();

        Ferias ferias = new Ferias();
        ferias.setNome(nome);
        ferias.setAprovado(aprovado);
        ferias.setDataInicial(dataFormatadaInicial);
        ferias.setDataFinal(dataFormatadaFinal);
        ferias.setQuantidade(tempo);
        ferias.setObservacao(observacao);

        return ferias;

    }
}
