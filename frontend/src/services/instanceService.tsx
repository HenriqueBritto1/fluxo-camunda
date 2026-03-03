import {api} from "./Api.tsx";

export interface SolicitarFeriasRequest {
    nome: string
    dataInicio: string;
    quantidadeDias: string;
    setor: string;
    email: string;
}

export const InstanceService = {
    startInstance: async () => {
        try {
            const response = await api.post("/engine-rest/process-definition/key/fluxo-process/start", {})
            return response.data;
        }catch (e) {
            console.error("Erro ao iniciar processo:", e);
            alert("Falha ao iniciar pedido.");
        }
    },

    closeInstance: async (id: string) =>{
        try {
            const response = await api.delete(`/engine-rest/process-instance/${id}`)
            return response.data;
        }catch (e) {
            console.error("Erro ao cancelar processo:", e);
            throw e;
        }
    },

    claimTask: async(id: string, user: string) =>{
        try {
            const response = await api.post(`/engine-rest/task/${id}/claim`, {userId: user})
            return response.data;
        }catch (e) {
            console.error("Erro ao dar assignment para a task:", e);
            throw e;
        }
    },

    submitForm: async (id: string, request: SolicitarFeriasRequest) => {
        //const dataFormatada = request.dataInicio.split('-').reverse().join('/');
        try {
            const payload = {
                variables: {
                    nome: { value: request.nome, type: "String" },
                    data_ferias: { value: request.dataInicio, type: "String"},
                    tempo_ferias: { value: request.quantidadeDias, type: "String" },
                    setor: { value: request.setor, type: "String" },
                    email: { value: request.email, type: "String" }
                }
            }
            const response = await api.post(`/engine-rest/task/${id}/complete`, payload)
            return response.data;
        }catch (e) {
            console.error("Erro ao enviar formulario:", e);
            throw e;
        }
    },

    getTaskId: async (processInstanceId: string): Promise<string> => {
        try {
            const response = await api.get(`/engine-rest/task?processInstanceId=${processInstanceId}`);

            if (response.data && response.data.length > 0) {
                return response.data[0].id;
            } else {
                throw new Error("Nenhuma tarefa ativa encontrada para esta instância.");
            }
        } catch (error) {
            console.error("Erro ao buscar Task ID:", error);
            throw error;
        }
    }

}