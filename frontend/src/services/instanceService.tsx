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
    }
}