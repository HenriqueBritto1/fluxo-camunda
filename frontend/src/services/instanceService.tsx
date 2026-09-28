import { api } from "./Api.tsx";

export interface SolicitacaoFeriasDados {
    nome: string;
    dataInicio: string;
    quantidadeDias: string;
    setor: string;
    email: string;
}

export interface ValidacaoFeriasDados {
    nome: string;
    dataInicio: string;
    quantidadeDias: string;
    aprovado: boolean;
    observacaoGestor: string;
}

const CAMUNDA_BASE_PATH = "/engine-rest";
const PROCESS_KEY = "fluxo-process";

const mapearParaVariaveisCamunda = (dados: any) => {
    const variaveis: Record<string, any> = {};
    for (const key in dados) {
        let type = "String";
        if (typeof dados[key] === "boolean") type = "Boolean";
        else if (typeof dados[key] === "number") type = "Integer";
        
        variaveis[key] = { value: dados[key], type };
    }
    return variaveis;
};

const mapearDeVariaveisCamunda = (variaveis: Record<string, any>) => {
    const resultado: Record<string, any> = {};
    for (const key in variaveis) {
        resultado[key] = variaveis[key]?.value;
    }
    return resultado;
};

export const ProcessoFeriasService = {
    iniciarProcessoDeFerias: async (): Promise<string> => {
        const response = await api.post(`${CAMUNDA_BASE_PATH}/process-definition/key/${PROCESS_KEY}/start`, {});
        return response.data.id;
    },

    cancelarProcessoDeFerias: async (instanciaId: string): Promise<void> => {
        await api.delete(`${CAMUNDA_BASE_PATH}/process-instance/${instanciaId}`);
    },

    atribuirTarefa: async (tarefaId: string, usuarioId: string): Promise<void> => {
        await api.post(`${CAMUNDA_BASE_PATH}/task/${tarefaId}/claim`, { userId: usuarioId });
    },

    enviarFormularioSolicitacao: async (tarefaId: string, dadosSolicitacao: SolicitacaoFeriasDados): Promise<void> => {
        const payload = {
            variables: mapearParaVariaveisCamunda({
                nome: dadosSolicitacao.nome,
                data_ferias: dadosSolicitacao.dataInicio,
                tempo_ferias: dadosSolicitacao.quantidadeDias,
                setor: dadosSolicitacao.setor,
                email: dadosSolicitacao.email
            })
        };
        await api.post(`${CAMUNDA_BASE_PATH}/task/${tarefaId}/complete`, payload);
    },

    obterTarefaAtivaId: async (instanciaProcessoId: string): Promise<string> => {
        const response = await api.get(`${CAMUNDA_BASE_PATH}/task?processInstanceId=${instanciaProcessoId}`);
        if (response.data && response.data.length > 0) {
            return response.data[0].id;
        }
        throw new Error("Nenhuma tarefa ativa encontrada para esta instância.");
    },

    buscarTarefasDisponiveisPorGrupo: async (grupoCandidato: string) => {
        const response = await api.get(`${CAMUNDA_BASE_PATH}/task?candidateGroup=${grupoCandidato}`);
        return response.data;
    },

    buscarTarefasAtribuidas: async (usuarioResponsavel: string) => {
        const response = await api.get(`${CAMUNDA_BASE_PATH}/task?assignee=${usuarioResponsavel}`);
        return response.data;
    },

    enviarValidacaoFerias: async (tarefaId: string, dadosValidacao: ValidacaoFeriasDados): Promise<void> => {
        const payload = {
            variables: mapearParaVariaveisCamunda({
                nome: dadosValidacao.nome,
                data_ferias: dadosValidacao.dataInicio,
                tempo_ferias: dadosValidacao.quantidadeDias,
                aprovado: dadosValidacao.aprovado,
                obs: dadosValidacao.observacaoGestor
            })
        };
        await api.post(`${CAMUNDA_BASE_PATH}/task/${tarefaId}/complete`, payload);
    },

    obterVariaveisDoProcesso: async (instanciaId: string): Promise<Record<string, any>> => {
        const response = await api.get(`${CAMUNDA_BASE_PATH}/process-instance/${instanciaId}/variables`);
        return mapearDeVariaveisCamunda(response.data);
    }
};