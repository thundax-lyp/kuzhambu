import { BrowserRouter, Route, Routes } from "react-router-dom";
import { HomePage } from "@/features/home/home-page";
import { ShareListPage } from "@/features/shares/share-list-page";
import { SharePage } from "@/features/shares/share-page";

import "./styles.css";

export function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/shares" element={<ShareListPage />} />
                <Route path="/share/:shareToken" element={<SharePage />} />
            </Routes>
        </BrowserRouter>
    );
}
