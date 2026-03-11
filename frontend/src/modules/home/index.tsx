import '../../App.css'
import {InstanceService} from "../../services/instanceService";
import FormRequest from '../../components/FormRequest.tsx'
import {useState} from "react";

export const HomePage: React.FC = () => {
    const [openModal, setOpenModal] = useState(false);
    const [instanceId, setInstanceId] = useState("");
    const user = "admin"

    async function startProcess() {
        const data = await InstanceService.startInstance();
        setInstanceId(data.id);
        console.log(data.id);
        const realTaskId = await InstanceService.getTaskId(data.id);
        InstanceService.claimTask(realTaskId, user);
        setOpenModal(true);
    }

    return (
        <>
            <div>
                <FormRequest
                    isOpen={openModal}
                    instanceId={instanceId}
                    onClose={() => {
                        setOpenModal(false)
                    }}
                />
            </div>
            <div className="title-div" style={{padding: 10}}><h1>Iniciar pedido de férias</h1></div>
            <div className="card">
                <button onClick={startProcess} className="start-1">
                    Start
                </button>
            </div>
        </>
    )
}