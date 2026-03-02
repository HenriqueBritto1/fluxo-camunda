import {api} from "./Api.tsx";

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
    }
}