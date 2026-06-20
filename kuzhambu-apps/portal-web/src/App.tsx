import { BrowserRouter, Route, Routes } from "react-router-dom";
import { HomePage } from "@/features/home/home-page";
import { ShareListPage } from "@/features/shares/share-list-page";
import { SharePage } from "@/features/shares/share-page";

import "./styles.css";

const normalizeRouterBasename = (baseUrl: string) => {
    const normalizedBaseUrl = baseUrl.replace(/\/+$/, "");
    return normalizedBaseUrl === "" ? undefined : normalizedBaseUrl;
};

export function App() {
    return (
        <BrowserRouter basename={normalizeRouterBasename(import.meta.env.BASE_URL)}>
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/shares" element={<ShareListPage />} />
                <Route path="/share/:shareToken" element={<SharePage />} />
            </Routes>
        </BrowserRouter>
    );
}
