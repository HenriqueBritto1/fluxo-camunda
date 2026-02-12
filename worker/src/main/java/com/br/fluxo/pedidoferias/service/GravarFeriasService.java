package com.br.fluxo.pedidoferias.service;

import com.br.fluxo.pedidoferias.common.TempoFeriasEnum;
import com.br.fluxo.pedidoferias.model.Ferias;
import com.br.fluxo.pedidoferias.repository.FeriasRepository;
import org.camunda.bpm.client.task.ExternalTask;
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

    public Ferias getVariaveisFerias(ExternalTask externalTask) {

        String nome = externalTask.getVariable("nome");

        Date data = externalTask.getVariable("data_ferias");
        LocalDate dataFormatadaInicial = data.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        TempoFeriasEnum tempo = TempoFeriasEnum.valueOf(externalTask.getVariable("tempo_ferias"));

        LocalDate dataFormatadaFinal = dataFormatadaInicial.plusDays(tempo.ordinal());

        Boolean aprovado = externalTask.getVariable("aprovado");

        Ferias ferias = new Ferias();
        ferias.setNome(nome);
        ferias.setAprovado(aprovado);
        ferias.setDataInicial(dataFormatadaInicial);
        ferias.setDataFinal(dataFormatadaFinal);
        ferias.setQuantidade(tempo);

        return ferias;

    }
}
