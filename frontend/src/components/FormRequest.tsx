import type {FormEvent} from "react";
import { InstanceService } from "../services/instanceService";

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    //children: ReactNode;
    instanceId: string;
}

export default function FormRequest({isOpen, onClose, title, instanceId}: ModalProps){


    const handleSubmit = (e: FormEvent) => {
        e.preventDefault();

        console.log("Dados enviados!");
        onClose();
    };

    return(
        isOpen ?
            <div className="modal-overlay">
                <div className="modal-content">
                    <h2>Solicitação de Férias</h2>
                    <form onSubmit={handleSubmit}>
                        {title}
                        <div className="form-group">
                            <label>Nome completo:</label>
                            <input type="text"/>
                        </div>
                        <div className="form-group">
                            <label>Email:</label>
                            <input type="text"/>
                        </div>
                        <div className="form-group">
                            <label>Setor:</label>
                            <input type="text"/>
                        </div>
                        <div className="form-group">
                            <label>Data de Início:</label>
                            <input type="date" required />
                        </div>

                        <div className="form-group">
                            <label>Quantidade de Dias:</label>
                            <select name="quantidade-dias">
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