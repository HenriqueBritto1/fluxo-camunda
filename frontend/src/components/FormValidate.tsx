import { useEffect, useState } from "react";
import { ProcessoFeriasService, type ValidacaoFeriasDados } from "../services/instanceService.tsx";

interface ValidacaoFeriasModalProps {
    isModalVisible: boolean;
    fecharModal: () => void;
    instanciaProcessoId: string;
    recarregarTarefas: () => void;
}

const TEMPO_FERIAS_PADRAO = "SETE_DIAS";

export default function FormValidate({ isModalVisible, fecharModal, instanciaProcessoId, recarregarTarefas }: ValidacaoFeriasModalProps) {
    const [dadosValidacao, setDadosValidacao] = useState<ValidacaoFeriasDados>({
        nome: "",
        dataInicio: "",
        quantidadeDias: TEMPO_FERIAS_PADRAO,
        aprovado: false,
        observacaoGestor: ""
    });

    const carregarDadosDoProcesso = async (instanciaId: string) => {
        try {
            const variaveis = await ProcessoFeriasService.obterVariaveisDoProcesso(instanciaId);
            setDadosValidacao({
                nome: variaveis.nome || "",
                dataInicio: variaveis.data_ferias || "",
                quantidadeDias: variaveis.tempo_ferias || TEMPO_FERIAS_PADRAO,
                aprovado: false,
                observacaoGestor: variaveis.obs || ""
            });
        } catch (erro) {
            console.error("Erro ao carregar dados:", erro);
        }
    };

    useEffect(() => {
        if (isModalVisible && instanciaProcessoId) {
            carregarDadosDoProcesso(instanciaProcessoId);
        }
    }, [isModalVisible, instanciaProcessoId]);

    const executarDecisao = async (isAprovado: boolean) => {
        try {
            const tarefaId = await ProcessoFeriasService.obterTarefaAtivaId(instanciaProcessoId);
            
            const dadosParaEnviar = {
                ...dadosValidacao,
                aprovado: isAprovado
            };
            
            await ProcessoFeriasService.enviarValidacaoFerias(tarefaId, dadosParaEnviar);
            
            recarregarTarefas();
            alert("Validação enviada com sucesso!");
            fecharModal();
        } catch (erro) {
            console.error("Erro ao enviar validação:", erro);
            alert("Falha ao enviar validação.");
        }
    };

    if (!isModalVisible) return null;

    return (
        <div className="modal-overlay" onClick={fecharModal}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="title-div">
                    <h2>Validação de Solicitação de Férias</h2>
                </div>
                <form onSubmit={(e) => e.preventDefault()}>
                    <div className="form-group">
                        <label>Nome completo:</label>
                        <input 
                            type="text" 
                            value={dadosValidacao.nome} 
                            readOnly 
                        />
                    </div>
                    <div className="form-group">
                        <label>Data de Início:</label>
                        <input 
                            type="date" 
                            value={dadosValidacao.dataInicio} 
                            readOnly 
                        />
                    </div>
                    <div className="form-group">
                        <label>Quantidade de Dias:</label>
                        <input 
                            value={dadosValidacao.quantidadeDias} 
                            readOnly 
                        />
                    </div>
                    <div className="form-group">
                        <label>Observação do Gestor:</label>
                        <input 
                            type="text" 
                            value={dadosValidacao.observacaoGestor} 
                            onChange={(e) => setDadosValidacao({ ...dadosValidacao, observacaoGestor: e.target.value })} 
                        />
                    </div>

                    <div className="modal-actions">
                        <button 
                            type="button" 
                            className="btn-submit-reprovado" 
                            onClick={() => executarDecisao(false)}
                        >
                            Reprovar
                        </button>
                        <button 
                            type="button" 
                            className="btn-submit-aprovado" 
                            onClick={() => executarDecisao(true)}
                        >
                            Aprovar
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}