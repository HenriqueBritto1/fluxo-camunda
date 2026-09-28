import '../../App.css'
import { ProcessoFeriasService } from "../../services/instanceService.tsx";
import FormRequest from '../../components/FormRequest.tsx'
import { useState } from "react";

const USUARIO_SISTEMA = "admin";

export const HomePage: React.FC = () => {
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [instanciaProcessoId, setInstanciaProcessoId] = useState("");

    const iniciarProcesso = async () => {
        try {
            const instanciaId = await ProcessoFeriasService.iniciarProcessoDeFerias();
            setInstanciaProcessoId(instanciaId);
            
            const tarefaAtivaId = await ProcessoFeriasService.obterTarefaAtivaId(instanciaId);
            await ProcessoFeriasService.atribuirTarefa(tarefaAtivaId, USUARIO_SISTEMA);
            
            setIsModalVisible(true);
        } catch (erro) {
            console.error("Erro no fluxo de inicialização:", erro);
            alert("Não foi possível iniciar o pedido de férias.");
        }
    };

    return (
        <>
            <div>
                <FormRequest
                    isModalVisible={isModalVisible}
                    instanciaProcessoId={instanciaProcessoId}
                    fecharModal={() => setIsModalVisible(false)}
                />
            </div>
            <div className="title-div" style={{ padding: 10 }}>
                <h1>Iniciar Pedido de Férias</h1>
            </div>
            <div className="card">
                <button onClick={iniciarProcesso} className="start-1">
                    Start
                </button>
            </div>
        </>
    );
};