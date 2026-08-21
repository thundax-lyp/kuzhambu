import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KnowledgeQualityPage } from "./quality-page";

vi.mock("./quality-service", () => ({
    KNOWLEDGE_QUALITY_FALLBACK: {
        focusIssues: [
            {
                href: "/knowledge/atlas",
                severity: "warning",
                summary: "帝系关系仍有一批待确认记录，建议优先治理高频人物关系。",
                title: "帝系关系仍需补齐确认"
            }
        ],
        qualityStats: [
            {
                deltaText: "人工确认后的核心实体占比",
                key: "entity-confirmed-rate",
                label: "实体确认率",
                statusTone: "stable",
                unit: "%",
                value: "82"
            }
        ],
        sourceBreakdowns: [],
        sourceDetails: [
            {
                href: "/knowledge/atlas",
                sourceTitle: "三才图会",
                sourceType: "SANCAI_ENTRY",
                status: "APPLIED",
                updatedAt: null
            }
        ],
        trendSeries: [
            {
                points: [{ label: "6月", value: 34 }],
                seriesKey: "new-tags",
                seriesLabel: "近三月新增标签"
            }
        ]
    },
    getKnowledgeQuality: async () => ({
        focusIssues: [
            {
                href: "/knowledge/atlas",
                severity: "warning",
                summary: "帝系关系仍有一批待确认记录，建议优先治理高频人物关系。",
                title: "帝系关系仍需补齐确认"
            }
        ],
        qualityStats: [
            {
                deltaText: "人工确认后的核心实体占比",
                key: "entity-confirmed-rate",
                label: "实体确认率",
                statusTone: "stable",
                unit: "%",
                value: "82"
            }
        ],
        sourceBreakdowns: [],
        sourceDetails: [
            {
                href: "/knowledge/atlas",
                sourceTitle: "三才图会",
                sourceType: "SANCAI_ENTRY",
                status: "APPLIED",
                updatedAt: null
            }
        ],
        trendSeries: [
            {
                points: [{ label: "6月", value: 34 }],
                seriesKey: "new-tags",
                seriesLabel: "近三月新增标签"
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
                    <KnowledgeQualityPage />
                </MemoryRouter>
            </QueryClientProvider>
        );
    });

    return { container, root };
};

describe("KnowledgeQualityPage", () => {
    afterEach(() => {
        document.body.innerHTML = "";
    });

    it("shows stats trends issues and source details", () => {
        const { container, root } = renderPage();

        expect(container.textContent).toContain("质量总览台");
        expect(container.textContent).toContain("实体确认率");
        expect(container.textContent).toContain("近期变化");
        expect(container.textContent).toContain("建议优先处理");
        expect(container.textContent).toContain("最近来源快照");

        const links = Array.from(container.querySelectorAll("a")).map((link) =>
            link.getAttribute("href")
        );
        expect(links).toContain("/knowledge/atlas");

        act(() => {
            root.unmount();
        });
    });
});
