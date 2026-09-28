import type { CamundaTask } from "../../types/task.ts";
import { useEffect, useState } from "react";
import { ProcessoFeriasService } from "../../services/instanceService.tsx";
import FormValidate from "../../components/FormValidate.tsx";

const GRUPO_GESTOR = "gestor";
const USUARIO_GESTOR = "admin";

export const Tasklist: React.FC = () => {
    const [tarefasDisponiveis, setTarefasDisponiveis] = useState<CamundaTask[]>([]);
    const [minhasTarefas, setMinhasTarefas] = useState<CamundaTask[]>([]);
    const [isModalVisivel, setIsModalVisivel] = useState(false);
    const [instanciaSelecionadaId, setInstanciaSelecionadaId] = useState<string>("");

    const carregarTarefas = async () => {
        try {
            const disponíveis = await ProcessoFeriasService.buscarTarefasDisponiveisPorGrupo(GRUPO_GESTOR);
            const atribuidas = await ProcessoFeriasService.buscarTarefasAtribuidas(USUARIO_GESTOR);

            setTarefasDisponiveis(disponíveis);
            setMinhasTarefas(atribuidas);
        } catch (erro) {
            console.error("Erro ao carregar tarefas:", erro);
        }
    };

    const assumirTarefa = async (tarefaId: string) => {
        try {
            await ProcessoFeriasService.atribuirTarefa(tarefaId, USUARIO_GESTOR);
            await carregarTarefas();
        } catch (erro) {
            console.error("Erro ao assumir tarefa:", erro);
            alert("Não foi possível assumir a tarefa.");
        }
    };

    const abrirModalDeValidacao = (instanciaProcessoId: string) => {
        setInstanciaSelecionadaId(instanciaProcessoId);
        setIsModalVisivel(true);
    };

    useEffect(() => {
        carregarTarefas();
    }, []);

    return (
        <>
            <div style={{ padding: 20 }}>
                <div className="title-div" style={{ padding: 10 }}>
                    <h1>Tasklist Customizada</h1>
                </div>

                <h2 style={{ color: "#032A28" }}>Tarefas Disponíveis</h2>
                {tarefasDisponiveis.map(tarefa => (
                    <div key={tarefa.id} className="task">
                        <p><strong>{tarefa.name}</strong></p>
                        <button className="btn-task" onClick={() => assumirTarefa(tarefa.id)}>
                            Claim
                        </button>
                    </div>
                ))}

                <h2 style={{ color: "#032A28" }}>Minhas Tarefas</h2>
                {minhasTarefas.map(tarefa => (
                    <div key={tarefa.id} className="task">
                        <p><strong>{tarefa.name}</strong></p>
                        <button 
                            className="btn-task" 
                            onClick={() => abrirModalDeValidacao(tarefa.processInstanceId)}
                        >
                            Validar
                        </button>
                    </div>
                ))}
                
                <FormValidate
                    isModalVisible={isModalVisivel}
                    instanciaProcessoId={instanciaSelecionadaId}
                    fecharModal={() => {
                        setIsModalVisivel(false);
                        setInstanciaSelecionadaId("");
                    }}
                    recarregarTarefas={carregarTarefas}
                />
            </div>
        </>
    );
};