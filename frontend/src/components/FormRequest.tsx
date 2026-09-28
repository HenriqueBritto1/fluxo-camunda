import { type FormEvent, useState } from "react";
import { ProcessoFeriasService, type SolicitacaoFeriasDados } from "../services/instanceService.tsx";

interface SolicitacaoFeriasModalProps {
    isModalVisible: boolean;
    fecharModal: () => void;
    instanciaProcessoId: string;
}

const TEMPO_FERIAS_PADRAO = "SETE_DIAS";

export default function FormRequest({ isModalVisible, fecharModal, instanciaProcessoId }: SolicitacaoFeriasModalProps) {
    const [dadosFormulario, setDadosFormulario] = useState<SolicitacaoFeriasDados>({
        nome: "",
        email: "",
        setor: "",
        dataInicio: "",
        quantidadeDias: TEMPO_FERIAS_PADRAO
    });

    const executarEnvio = async (evento: FormEvent) => {
        evento.preventDefault();
        try {
            const tarefaId = await ProcessoFeriasService.obterTarefaAtivaId(instanciaProcessoId);
            await ProcessoFeriasService.enviarFormularioSolicitacao(tarefaId, dadosFormulario);
            alert("Solicitação de férias enviada com sucesso!");
            fecharModal();
        } catch (erro) {
            console.error("Erro ao enviar formulario:", erro);
            alert("Falha ao enviar solicitação.");
        }
    };

    const cancelarSolicitacao = async () => {
        try {
            await ProcessoFeriasService.cancelarProcessoDeFerias(instanciaProcessoId);
            fecharModal();
        } catch (erro) {
            console.error("Erro ao cancelar processo:", erro);
            alert("Falha ao cancelar o processo.");
        }
    };

    if (!isModalVisible) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <div className="title-div">
                    <h2>Solicitação de Férias</h2>
                </div>
                <form onSubmit={executarEnvio}>
                    <div className="form-group">
                        <label>Nome completo:</label>
                        <input 
                            type="text" 
                            value={dadosFormulario.nome} 
                            onChange={(e) => setDadosFormulario({ ...dadosFormulario, nome: e.target.value })} 
                            required 
                        />
                    </div>
                    <div className="form-group">
                        <label>Email:</label>
                        <input 
                            type="email" 
                            value={dadosFormulario.email} 
                            onChange={(e) => setDadosFormulario({ ...dadosFormulario, email: e.target.value })} 
                            required 
                        />
                    </div>
                    <div className="form-group">
                        <label>Setor:</label>
                        <input 
                            type="text" 
                            value={dadosFormulario.setor} 
                            onChange={(e) => setDadosFormulario({ ...dadosFormulario, setor: e.target.value })} 
                            required 
                        />
                    </div>
                    <div className="form-group">
                        <label>Data de Início:</label>
                        <input 
                            type="date" 
                            value={dadosFormulario.dataInicio} 
                            onChange={(e) => setDadosFormulario({ ...dadosFormulario, dataInicio: e.target.value })} 
                            required 
                        />
                    </div>
                    <div className="form-group">
                        <label>Quantidade de Dias:</label>
                        <select 
                            value={dadosFormulario.quantidadeDias} 
                            onChange={(e) => setDadosFormulario({ ...dadosFormulario, quantidadeDias: e.target.value })}
                        >
                            <option value="SETE_DIAS">7 dias</option>
                            <option value="QUINZE_DIAS">15 dias</option>
                            <option value="TRINTA_DIAS">30 dias</option>
                        </select>
                    </div>

                    <div className="modal-actions">
                        <button type="button" className="btn-cancel" onClick={cancelarSolicitacao}>
                            Cancelar
                        </button>
                        <button type="submit" className="btn-submit">
                            Enviar Pedido
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}