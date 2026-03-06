
import {BrowserRouter, Route, Routes} from "react-router-dom";
import {HomePage} from "./modules/home";
import {Tasklist} from "./modules/tasklist";

function App() {

    return (
    <BrowserRouter>
        <Routes>
            <Route path ="/" element={<HomePage />} />
            <Route path="/tasklist" element={<Tasklist />} />
        </Routes>
    </BrowserRouter>
        )
}

export default App
