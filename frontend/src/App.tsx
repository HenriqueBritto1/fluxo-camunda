import './App.css'
import { InstanceService } from "./services/instanceService";
import FormRequest from "./components/FormRequest.tsx"
import { useState } from "react";

function App() {
    const [openModal, setOpenModal] = useState(false);
    const [instanceId, setInstanceId] = useState("")

    async function startProcess() {
         const data = await InstanceService.startInstance();
         setInstanceId(data.id);
         console.log(data.id);
         const realTaskId = await InstanceService.getTaskId(data.id);
         InstanceService.claimTask(realTaskId, "admin");
         setOpenModal(true);
    }

    return (
    <>
      <div>
          <FormRequest
              isOpen={openModal}
              instanceId = {instanceId}
              onClose={()=>{setOpenModal(false)}}
              title={'processo iniciado:' + instanceId}
          />
      </div>
      <h1>Iniciar pedido de férias</h1>
      <div className="card">
        <button onClick = {startProcess}>
          Start
        </button>
      </div>
    </>
  )
}

export default App
