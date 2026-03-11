import {type FormEvent, useState} from "react";
import { InstanceService, type SolicitarFeriasRequest } from "../services/instanceService";

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    //children: ReactNode;
    instanceId: string;
}

export default function FormRequest({isOpen, onClose, instanceId}: ModalProps){
    const [formData, setFormData] = useState<SolicitarFeriasRequest>({
        nome: "",
        email: "",
        setor: "",
        dataInicio: "",
        quantidadeDias: "SETE_DIAS"
    });

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        const realTaskId = await InstanceService.getTaskId(instanceId);
        await InstanceService.submitForm(realTaskId, formData);
        console.log("Dados enviados!");
        onClose();
    };

    return(
        isOpen ?
            <div className="modal-overlay">
                <div className="modal-content">
                    <div className="title-div"><h2>Solicitação de Férias</h2></div>
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label>Nome completo:</label>
                            <input type="text" value={formData.nome} onChange={(e) => setFormData({...formData, nome: e.target.value})} required={true}/>
                        </div>
                        <div className="form-group">
                            <label>Email:</label>
                            <input type="email" value= {formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} required={true}/>
                        </div>
                        <div className="form-group">
                            <label>Setor:</label>
                            <input type="text" value={formData.setor} onChange={(e) => setFormData({...formData, setor: e.target.value})} required={true}/>
                        </div>
                        <div className="form-group">
                            <label>Data de Início:</label>
                            <input type="date" required  value={formData.dataInicio} onChange={(e) => setFormData({...formData, dataInicio: e.target.value})}/>
                        </div>

                        <div className="form-group">
                            <label>Quantidade de Dias:</label>
                            <select name="quantidade-dias" value={formData.quantidadeDias} onChange={(e) => setFormData({...formData, quantidadeDias: e.target.value})}>
                                <option value="SETE_DIAS">7 dias</option>
                                <option value="QUINZE_DIAS">15 dias</option>
                                <option value="TRINTA_DIAS">30 dias</option>
                            </select>
                        </div>

                        <div className="modal-actions">
                            <button type="button" className="btn-cancel" onClick={async () => {
                                InstanceService.closeInstance(instanceId)
                                onClose();
                            }}
                            >
                                Cancelar
                            </button>
                            <button type="submit" className="btn-submit">
                                Enviar Pedido
                            </button>
                        </div>
                    </form>
                </div>
            </div>
            : null
    );
}