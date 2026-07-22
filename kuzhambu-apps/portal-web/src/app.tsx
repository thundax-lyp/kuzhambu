import { BrowserRouter, Route, Routes } from "react-router-dom";
import { PortalLayout } from "@/components/portal-layout";
import { SancaiPage } from "@/pages/classics/sancai-page";
import { DiscoveryQaPage } from "@/pages/discovery/qa-page";
import { DiscoverySearchItemPage } from "@/pages/discovery/search-item-page";
import { DiscoverySearchPage } from "@/pages/discovery/search-page";
import { HomePage } from "@/pages/home/home-page";
import { KnowledgeAtlasPage } from "@/pages/knowledge/knowledge-atlas-page";
import { KnowledgeHomePage } from "@/pages/knowledge/knowledge-home-page";
import { KnowledgeLineagePage } from "@/pages/knowledge/knowledge-lineage-page";
import { KnowledgeQualityPage } from "@/pages/knowledge/knowledge-quality-page";
import { ShareDetailPage } from "@/pages/share-detail/share-detail-page";
import { ShareListPage } from "@/pages/share-list/share-list-page";

import "./styles.css";
import "@/pages/knowledge/knowledge-page.css";

const normalizeRouterBasename = (baseUrl: string) => {
    const normalizedBaseUrl = baseUrl.replace(/\/+$/, "");
    return normalizedBaseUrl === "" ? undefined : normalizedBaseUrl;
};

export const App = () => {
    return (
        <BrowserRouter basename={normalizeRouterBasename(import.meta.env.BASE_URL)}>
            <Routes>
                <Route element={<PortalLayout />}>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/knowledge" element={<KnowledgeHomePage />} />
                    <Route path="/knowledge/atlas" element={<KnowledgeAtlasPage />} />
                    <Route path="/knowledge/lineage" element={<KnowledgeLineagePage />} />
                    <Route path="/knowledge/quality" element={<KnowledgeQualityPage />} />
                    <Route path="/classics/sancai" element={<SancaiPage />} />
                    <Route path="/discovery/search" element={<DiscoverySearchPage />} />
                    <Route path="/discovery/search-item" element={<DiscoverySearchItemPage />} />
                    <Route path="/discovery/qa" element={<DiscoveryQaPage />} />
                    <Route path="/shares" element={<ShareListPage />} />
                    <Route path="/share/:shareToken" element={<ShareDetailPage />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
};
