import {type FormEvent, useEffect, useState} from "react";
import {InstanceService, type ValidarFeriasRequest} from "../services/instanceService.tsx";
import type {CamundaTask} from "../types/task.ts";

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    //children: ReactNode;
    instanceId: string;
    loadTasks: () => void;
}

export default function FormValidate({isOpen, onClose, instanceId, loadTasks}: ModalProps) {
    const [formData, setFormData] = useState<ValidarFeriasRequest>({
        nome: "",
        dataInicio: "",
        quantidadeDias: "SETE_DIAS",
        aprovado: false,
        obs: ""
    })

    function extractValues(variables: any) {
        const result: any = {};

        for (const key in variables) {
            result[key] = variables[key]?.value;
        }

        return result;
    }

    async function handleOpen(task: CamundaTask) {
        const vars = await InstanceService.getVariables(task.processInstanceId);
        const extractedVars = extractValues(vars);

        setFormData({
            nome: extractedVars.nome || "",
            dataInicio: extractedVars.data_ferias || "",
            quantidadeDias: extractedVars.tempo_ferias || "SETE_DIAS",
            aprovado: false,
            obs: extractedVars.obs || ""
        })
    }

    const handleDecision = async (aprovadoValue: boolean) => {
        try {
            const realTaskId = await InstanceService.getTaskId(instanceId);

            await InstanceService.submitValid(realTaskId, {
                ...formData,
                aprovado: aprovadoValue
            });

            loadTasks();
            console.log("Validação enviada!");
            onClose();
        } catch (error) {
            console.error("Erro ao enviar:", error);
        }
    };

    useEffect(() => {
        if (isOpen && instanceId) {
            handleOpen({ processInstanceId: instanceId } as CamundaTask);
        }
    }, [isOpen, instanceId]);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        const realTaskId = await InstanceService.getTaskId(instanceId);

        await InstanceService.submitValid(realTaskId, formData);

        onClose();
    };

    return(
        isOpen ?
            <div className="modal-overlay" onClick={onClose}>
                <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                    <div className="title-div"><h2>Validação de solicitação de férias</h2></div>
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label>Nome completo:</label>
                            <input type="text" value={formData.nome} onChange={(e) => setFormData({...formData, nome: e.target.value})} readOnly={true}/>
                        </div>
                        <div className="form-group">
                            <label>Data de Início:</label>
                            <input type="date" required  value={formData.dataInicio} onChange={(e) => setFormData({...formData, dataInicio: e.target.value})} readOnly={true}/>
                        </div>

                        <div className="form-group">
                            <label>Quantidade de Dias:</label>
                            <input name="quantidade-dias" value={formData.quantidadeDias} onChange={(e) => setFormData({...formData, quantidadeDias: e.target.value})} readOnly={true}/>
                        </div>
                        <div className="form-group">
                            <label>Observação:</label>
                            <input type="text" value={formData.obs} onChange={(e) => setFormData({...formData, obs: e.target.value})}/>
                        </div>

                        <div className="modal-actions">
                            <button type="button" className="btn-submit-reprovado" onClick={() => handleDecision(false)}>
                                Reprovar
                            </button>
                            <button type="button" className="btn-submit-aprovado" onClick={() => handleDecision(true)}>
                                Aprovar
                            </button>
                        </div>
                    </form>
                </div>
            </div>
            : null
    );

}