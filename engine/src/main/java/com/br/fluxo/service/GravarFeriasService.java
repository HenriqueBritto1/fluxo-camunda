package com.br.fluxo.service;

import com.br.fluxo.common.TempoFeriasEnum;
import com.br.fluxo.model.Ferias;
import com.br.fluxo.repository.FeriasRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class GravarFeriasService {

    private static final String FORMATO_DATA_PADRAO = "yyyy-MM-dd";
    private static final int AJUSTE_DIAS_FIM_FERIAS = 1;

    private static final String VARIAVEL_NOME_COLABORADOR = "nome";
    private static final String VARIAVEL_DATA_INICIO_FERIAS = "data_ferias";
    private static final String VARIAVEL_TEMPO_DE_FERIAS = "tempo_ferias";
    private static final String VARIAVEL_STATUS_APROVACAO = "aprovado";
    private static final String VARIAVEL_OBSERVACAO = "obs";

    private final FeriasRepository feriasRepository;

    @Autowired
    public GravarFeriasService(FeriasRepository feriasRepository) {
        this.feriasRepository = feriasRepository;
    }

    public void gravarFeriasDB(Ferias solicitacaoDeFerias) {
        feriasRepository.save(solicitacaoDeFerias);
    }

    public Ferias extrairFeriasDaExecucao(DelegateExecution execucaoDoProcesso) {
        String nomeColaborador = obterVariavelString(execucaoDoProcesso, VARIAVEL_NOME_COLABORADOR);
        String dataInicioString = obterVariavelString(execucaoDoProcesso, VARIAVEL_DATA_INICIO_FERIAS);
        String tempoFeriasString = obterVariavelString(execucaoDoProcesso, VARIAVEL_TEMPO_DE_FERIAS);
        Boolean aprovado = (Boolean) execucaoDoProcesso.getVariable(VARIAVEL_STATUS_APROVACAO);
        String observacao = obterVariavelString(execucaoDoProcesso, VARIAVEL_OBSERVACAO);

        LocalDate dataInicioFormatada = formatarData(dataInicioString);
        TempoFeriasEnum tempoFerias = TempoFeriasEnum.valueOf(tempoFeriasString);
        LocalDate dataFimFormatada = calcularDataFinal(dataInicioFormatada, tempoFerias);

        return construirObjetoFerias(nomeColaborador, dataInicioFormatada, dataFimFormatada, tempoFerias, aprovado, observacao);
    }

    private String obterVariavelString(DelegateExecution execucao, String nomeDaVariavel) {
        Object variavel = execucao.getVariable(nomeDaVariavel);
        return variavel != null ? variavel.toString() : "";
    }

    private LocalDate formatarData(String dataString) {
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern(FORMATO_DATA_PADRAO);
        return LocalDate.parse(dataString, formatador);
    }

    private LocalDate calcularDataFinal(LocalDate dataInicio, TempoFeriasEnum tempoFerias) {
        return dataInicio.plusDays(tempoFerias.getValor() - AJUSTE_DIAS_FIM_FERIAS);
    }

    private Ferias construirObjetoFerias(String nome, LocalDate dataInicio, LocalDate dataFim, TempoFeriasEnum tempoFerias, Boolean aprovado, String observacao) {
        Ferias ferias = new Ferias();
        ferias.setNome(nome);
        ferias.setDataInicial(dataInicio);
        ferias.setDataFinal(dataFim);
        ferias.setQuantidade(tempoFerias);
        ferias.setAprovado(Boolean.TRUE.equals(aprovado));
        ferias.setObservacao(observacao);
        return ferias;
    }
}
