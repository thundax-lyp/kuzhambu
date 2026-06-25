import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KnowledgeAtlasPage } from "./knowledge-atlas-page";

vi.mock("./knowledge-atlas-service", () => ({
    KNOWLEDGE_ATLAS_FALLBACK: {
        availableFilters: {
            entityTypes: ["人物", "器物"],
            knowledgeBases: ["三才图会"],
            relationTypes: ["帝系关系"],
            tagNames: ["上古"],
            timeRanges: ["最近 90 天"]
        },
        focusNode: {
            confidence: 0.95,
            coverImageUrl: null,
            id: "3001",
            status: "CONFIRMED",
            summary: "上古始祖",
            title: "黄帝",
            type: "PERSON"
        },
        relatedTags: [],
        relationGroups: [
            {
                groupKey: "ANCESTOR",
                groupLabel: "帝系关系",
                relations: [
                    {
                        relationLabel: "ANCESTOR",
                        relationType: "ANCESTOR",
                        sourceId: "person:huangdi",
                        sourceLabel: "黄帝",
                        targetId: "person:shaodian",
                        targetLabel: "少典",
                        weight: 0.95
                    }
                ]
            }
        ],
        sourceReferences: [],
        timelineItems: [
            {
                description: "该实体在图谱中首次被抽取并登记。",
                href: "/knowledge/atlas",
                timeLabel: "首次抽取",
                title: "知识首次进入图谱"
            }
        ]
    },
    getKnowledgeAtlas: async () => ({
        availableFilters: {
            entityTypes: ["人物", "器物"],
            knowledgeBases: ["三才图会"],
            relationTypes: ["帝系关系"],
            tagNames: ["上古"],
            timeRanges: ["最近 90 天"]
        },
        focusNode: {
            confidence: 0.95,
            coverImageUrl: null,
            id: "3001",
            status: "CONFIRMED",
            summary: "上古始祖",
            title: "黄帝",
            type: "PERSON"
        },
        relatedTags: [],
        relationGroups: [
            {
                groupKey: "ANCESTOR",
                groupLabel: "帝系关系",
                relations: [
                    {
                        relationLabel: "ANCESTOR",
                        relationType: "ANCESTOR",
                        sourceId: "person:huangdi",
                        sourceLabel: "黄帝",
                        targetId: "person:shaodian",
                        targetLabel: "少典",
                        weight: 0.95
                    }
                ]
            }
        ],
        sourceReferences: [],
        timelineItems: [
            {
                description: "该实体在图谱中首次被抽取并登记。",
                href: "/knowledge/atlas",
                timeLabel: "首次抽取",
                title: "知识首次进入图谱"
            }
        ]
    })
}));

const renderPage = () => {
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                gcTime: 0,
                retry: false
            }
        }
    });

    act(() => {
        root.render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter>
                    <KnowledgeAtlasPage />
                </MemoryRouter>
            </QueryClientProvider>
        );
    });

    return { container, root };
};

describe("KnowledgeAtlasPage", () => {
    afterEach(() => {
        document.body.innerHTML = "";
    });

    it("shows the filter browse detail structure", () => {
        const { container, root } = renderPage();

        expect(container.textContent).toContain("图谱浏览台");
        expect(container.textContent).toContain("筛选");
        expect(container.textContent).toContain("浏览");
        expect(container.textContent).toContain("详情");
        expect(container.textContent).toContain("黄帝");

        const links = Array.from(container.querySelectorAll("a")).map((link) =>
            link.getAttribute("href")
        );
        expect(links).toContain("/knowledge");

        act(() => {
            root.unmount();
        });
    });
});
