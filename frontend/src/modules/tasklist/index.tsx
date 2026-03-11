import type {CamundaTask} from "../../types/task.ts";
import {useEffect, useState} from "react";
import {InstanceService} from "../../services/instanceService.tsx";
import FormValidate from "../../components/FormValidate.tsx";

export const Tasklist: React.FC = () => {
    const [availableTasks, setAvailableTasks] = useState<CamundaTask[]>([]);
    const [myTasks, setMyTasks] = useState<CamundaTask[]>([]);
    const [openModal, setOpenModal] = useState(false)
    const [selectedInstanceId, setSelectedInstanceId] = useState<string | null>(null);
    const user = "admin";

    async function loadTask(){
        try {
            const available = await InstanceService.getTasksList("gestor");
            const myTasks = await InstanceService.getMyTasks(user);

            setAvailableTasks(available);
            setMyTasks(myTasks);
        }catch (e) {
            console.error(e);
        }
    }

    async function handleClaim(taskId: string) {
        await InstanceService.claimTask(taskId, user);
        await loadTask();
    }

    async function openRequest (taskId: string){
        await InstanceService.getTaskId(taskId)
        setOpenModal(true);
    }

    useEffect(() => {
        loadTask();
    }, [user]);

    return(
        <>

            <div style={{ padding: 20 }}>
                <div className="title-div" style={{padding: 10}}>
                    <h1>Tasklist Custom</h1>
                </div>

                <h2 style={{color: "#032A28"}}>Tarefas Disponíveis</h2>
                {availableTasks.map(task => (
                    <div key={task.id} className="task" >
                        <p><strong>{task.name}</strong></p>
                        <button className="btn-task" onClick={() => handleClaim(task.id)}>
                            Claim
                        </button>
                    </div>
                ))}

                <h2 style={{color: "#032A28"}}>Minhas Tarefas</h2>
                {myTasks.map(task => (
                    <div key={task.id} className="task" >
                        <p><strong>{task.name}</strong></p>
                        <button className="btn-task"  onClick={() => {
                            openRequest(task.processInstanceId)
                            setSelectedInstanceId(task.processInstanceId);
                        }}>Validar</button>

                    </div>
                ))}
                <FormValidate
                    isOpen={openModal}
                    instanceId={selectedInstanceId!}
                    onClose={() => {
                        setOpenModal(false)
                        setSelectedInstanceId(null);
                    }}
                    loadTasks={loadTask}
                    title={'processo: ' + selectedInstanceId}
                />
            </div>

        </>
    )
}