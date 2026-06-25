import { BrowserRouter, Route, Routes } from "react-router-dom";
import { DiscoverySearchPage } from "@/pages/discovery/search-page";
import { DiscoveryQaPage } from "@/pages/discovery/qa-page";
import { HomePage } from "@/pages/home/home-page";
import { KnowledgeAtlasPage } from "@/pages/knowledge/knowledge-atlas-page";
import { KnowledgeHomePage } from "@/pages/knowledge/knowledge-home-page";
import { KnowledgeQualityPage } from "@/pages/knowledge/knowledge-quality-page";
import { ShareForm } from "@/pages/share/share-form";
import { SharePage } from "@/pages/share/share-page";

import "./styles.css";

const normalizeRouterBasename = (baseUrl: string) => {
    const normalizedBaseUrl = baseUrl.replace(/\/+$/, "");
    return normalizedBaseUrl === "" ? undefined : normalizedBaseUrl;
};

export const App = () => {
    return (
        <BrowserRouter basename={normalizeRouterBasename(import.meta.env.BASE_URL)}>
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/knowledge" element={<KnowledgeHomePage />} />
                <Route path="/knowledge/atlas" element={<KnowledgeAtlasPage />} />
                <Route path="/knowledge/quality" element={<KnowledgeQualityPage />} />
                <Route path="/discovery/search" element={<DiscoverySearchPage />} />
                <Route path="/discovery/qa" element={<DiscoveryQaPage />} />
                <Route path="/shares" element={<SharePage />} />
                <Route path="/share/:shareToken" element={<ShareForm />} />
            </Routes>
        </BrowserRouter>
    );
};
