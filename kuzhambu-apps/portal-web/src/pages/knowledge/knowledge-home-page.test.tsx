import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KnowledgeHomePage } from "./knowledge-home-page";

vi.mock("./knowledge-home-service", () => ({
    KNOWLEDGE_HOME_FALLBACK: {
        featureCollections: [],
        heroSubtitle:
            "把古籍中的人物、器物、礼制与来源脉络组织成可阅读的知识展陈。这里不是治理后台，而是面向浏览与理解的知识入口。",
        heroTitle: "古籍知识图谱馆",
        quickLinks: [
            {
                description: "进入关系画布，沿实体、关系与时间线展开阅读。",
                href: "/knowledge/atlas",
                key: "atlas",
                label: "图谱浏览",
                type: "atlas"
            },
            {
                description: "进入人物谱系与亲缘关系画布。",
                href: "/knowledge/lineage",
                key: "lineage",
                label: "世系图浏览",
                type: "lineage"
            },
            {
                description: "查看确认率、来源构成与当前待处理事项。",
                href: "/knowledge/quality",
                key: "quality",
                label: "质量总览",
                type: "quality"
            }
        ],
        recentUpdates: [],
        searchPlaceholder: "人物 · 器物 · 礼制 · 典故 · 版本",
        stats: []
    },
    getKnowledgeHome: async () => ({
        featureCollections: [],
        heroSubtitle:
            "把古籍中的人物、器物、礼制与来源脉络组织成可阅读的知识展陈。这里不是治理后台，而是面向浏览与理解的知识入口。",
        heroTitle: "古籍知识图谱馆",
        quickLinks: [
            {
                description: "进入关系画布，沿实体、关系与时间线展开阅读。",
                href: "/knowledge/atlas",
                key: "atlas",
                label: "图谱浏览",
                type: "atlas"
            },
            {
                description: "进入人物谱系与亲缘关系画布。",
                href: "/knowledge/lineage",
                key: "lineage",
                label: "世系图浏览",
                type: "lineage"
            },
            {
                description: "查看确认率、来源构成与当前待处理事项。",
                href: "/knowledge/quality",
                key: "quality",
                label: "质量总览",
                type: "quality"
            }
        ],
        recentUpdates: [],
        searchPlaceholder: "人物 · 器物 · 礼制 · 典故 · 版本",
        stats: []
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
                    <KnowledgeHomePage />
                </MemoryRouter>
            </QueryClientProvider>
        );
    });

    return { container, root };
};

describe("KnowledgeHomePage", () => {
    afterEach(() => {
        document.body.innerHTML = "";
    });

    it("shows the atlas and quality entry points", () => {
        const { container, root } = renderPage();

        expect(container.textContent).toContain("古籍知识图谱馆");
        expect(container.textContent).toContain("图谱浏览");
        expect(container.textContent).toContain("世系图浏览");
        expect(container.textContent).toContain("质量总览");

        const links = Array.from(container.querySelectorAll("a")).map((link) => ({
            href: link.getAttribute("href"),
            text: link.textContent
        }));

        expect(links).toEqual(
            expect.arrayContaining([
                expect.objectContaining({ href: "/knowledge/atlas", text: "进入图谱浏览" }),
                expect.objectContaining({ href: "/knowledge/quality", text: "查看质量总览" }),
                expect.objectContaining({
                    href: "/knowledge/atlas",
                    text: "图谱浏览进入关系画布，沿实体、关系与时间线展开阅读。"
                }),
                expect.objectContaining({
                    href: "/knowledge/lineage",
                    text: "世系图浏览进入人物谱系与亲缘关系画布。"
                }),
                expect.objectContaining({
                    href: "/knowledge/quality",
                    text: "质量总览查看确认率、来源构成与当前待处理事项。"
                })
            ])
        );

        act(() => {
            root.unmount();
        });
    });
});
