import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KnowledgeAtlasPage } from "./knowledge-atlas-page";

const { getKnowledgeAtlas } = vi.hoisted(() => ({
    getKnowledgeAtlas: vi.fn()
}));

vi.mock("./knowledge-atlas-service", () => ({
    KNOWLEDGE_ATLAS_FALLBACK: {
        availableFilters: {
            entityTypes: ["PERSON", "CREATURE"],
            knowledgeBases: ["SANCAI_ENTRY"],
            relationTypes: ["ANCESTOR"],
            tagNames: ["上古"],
            timeRanges: ["90d"]
        },
        breadcrumbItems: [
            { href: "/knowledge/atlas?level=overview", label: "图谱总览", level: "overview" }
        ],
        categoryView: null,
        currentLevel: "overview",
        detailView: null,
        overviewView: {
            categoryCards: [
                {
                    appliedVersionCount: 2,
                    categoryCode: "BIRDS",
                    categoryName: "羽族",
                    entityCount: 2,
                    entryHref: "/knowledge/atlas?level=category&categoryCode=BIRDS",
                    latestVersionNo: 3,
                    relationCount: 1
                }
            ],
            summarySubtitle: "先看门类分布，再进入单门类浏览与单实体详情。",
            summaryTitle: "十四门类知识鸟瞰"
        }
    },
    getKnowledgeAtlas
}));

const renderPage = (initialEntry = "/knowledge/atlas") => {
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
                <MemoryRouter initialEntries={[initialEntry]}>
                    <KnowledgeAtlasPage />
                </MemoryRouter>
            </QueryClientProvider>
        );
    });

    return { container, root };
};

const flushQuery = async () => {
    await act(async () => {
        await Promise.resolve();
        await new Promise((resolve) => {
            window.setTimeout(resolve, 0);
        });
        await Promise.resolve();
        await new Promise((resolve) => {
            window.setTimeout(resolve, 0);
        });
    });
};

describe("KnowledgeAtlasPage", () => {
    afterEach(() => {
        document.body.innerHTML = "";
        getKnowledgeAtlas.mockReset();
    });

    it("restores overview level from url state", async () => {
        getKnowledgeAtlas.mockResolvedValue({
            availableFilters: {
                entityTypes: ["PERSON", "CREATURE"],
                knowledgeBases: ["SANCAI_ENTRY"],
                relationTypes: ["ANCESTOR"],
                tagNames: ["上古"],
                timeRanges: ["90d"]
            },
            breadcrumbItems: [
                { href: "/knowledge/atlas?level=overview", label: "图谱总览", level: "overview" }
            ],
            categoryView: null,
            currentLevel: "overview",
            detailView: null,
            overviewView: {
                categoryCards: [
                    {
                        appliedVersionCount: 2,
                        categoryCode: "BIRDS",
                        categoryName: "羽族",
                        entityCount: 2,
                        entryHref: "/knowledge/atlas?level=category&categoryCode=BIRDS",
                        latestVersionNo: 3,
                        relationCount: 1
                    }
                ],
                summarySubtitle: "先看门类分布，再进入单门类浏览与单实体详情。",
                summaryTitle: "十四门类知识鸟瞰"
            }
        });

        const { container, root } = renderPage("/knowledge/atlas?level=overview");
        await flushQuery();

        expect(container.textContent).toContain("图谱浏览台");
        expect(container.textContent).toContain("总览卷宗");
        expect(container.textContent).toContain("十四门类知识鸟瞰");
        expect(container.textContent).toContain("应用版本");
        expect(container.textContent).toContain("进入门类");
        expect(getKnowledgeAtlas).toHaveBeenCalledWith({
            categoryCode: null,
            entityId: null,
            level: "overview"
        });

        act(() => {
            root.unmount();
        });
    });

    it("restores category level from url state", async () => {
        getKnowledgeAtlas.mockResolvedValue({
            availableFilters: {
                entityTypes: ["CREATURE"],
                knowledgeBases: ["SANCAI_ENTRY"],
                relationTypes: ["KIN"],
                tagNames: ["鸟兽"],
                timeRanges: ["90d"]
            },
            breadcrumbItems: [
                { href: "/knowledge/atlas?level=overview", label: "图谱总览", level: "overview" },
                {
                    href: "/knowledge/atlas?level=category&categoryCode=BIRDS",
                    label: "羽族",
                    level: "category"
                }
            ],
            categoryView: {
                categoryCode: "BIRDS",
                categoryName: "羽族",
                entityHighlights: [
                    {
                        confirmationStatus: "CONFIRMED",
                        entityId: "3001",
                        entityName: "鸾",
                        entityType: "CREATURE",
                        entryHref: "/knowledge/atlas?level=detail&entityId=3001"
                    }
                ],
                latestVersionId: 71,
                latestVersionNo: 3,
                relationGroups: [
                    {
                        groupKey: "KIN",
                        groupLabel: "羽族关联",
                        relations: [
                            {
                                relationLabel: "KIN",
                                relationType: "KIN",
                                sourceId: "bird:luan",
                                sourceLabel: "鸾",
                                targetId: "bird:feng",
                                targetLabel: "凤",
                                weight: 0.92
                            }
                        ]
                    }
                ],
                sourceReferences: [
                    {
                        href: "/knowledge/atlas",
                        snippet: "当前门类来自最近一次已应用图谱版本，可继续进入单实体详情。",
                        sourceId: "1001",
                        sourceTitle: "羽族",
                        sourceType: "SANCAI_ENTRY",
                        updatedAt: null
                    }
                ]
            },
            currentLevel: "category",
            detailView: null,
            overviewView: null
        });

        const { container, root } = renderPage(
            "/knowledge/atlas?level=category&categoryCode=BIRDS"
        );
        await flushQuery();

        expect(container.textContent).toContain("羽族");
        expect(container.textContent).toContain("版本编号");
        expect(container.textContent).toContain("羽族关联");
        expect(container.textContent).toContain("当前门类来自最近一次已应用图谱版本");
        expect(container.textContent).toContain("进入详情");
        expect(getKnowledgeAtlas).toHaveBeenCalledWith({
            categoryCode: "BIRDS",
            entityId: null,
            level: "category"
        });

        act(() => {
            root.unmount();
        });
    });

    it("restores detail level from url state", async () => {
        getKnowledgeAtlas.mockResolvedValue({
            availableFilters: {
                entityTypes: ["PERSON"],
                knowledgeBases: ["SANCAI_ENTRY"],
                relationTypes: ["ANCESTOR"],
                tagNames: ["上古"],
                timeRanges: ["90d"]
            },
            breadcrumbItems: [
                { href: "/knowledge/atlas?level=overview", label: "图谱总览", level: "overview" },
                {
                    href: "/knowledge/atlas?level=category&categoryCode=BIRDS",
                    label: "羽族",
                    level: "category"
                },
                {
                    href: "/knowledge/atlas?level=detail&entityId=3001",
                    label: "黄帝",
                    level: "detail"
                }
            ],
            categoryView: null,
            currentLevel: "detail",
            detailView: {
                focusNode: {
                    confidence: 0.95,
                    coverImageUrl: null,
                    id: "3001",
                    status: "CONFIRMED",
                    summary: "上古始祖",
                    title: "黄帝",
                    type: "PERSON"
                },
                relatedTags: [{ score: 0.92, tagCategory: "时代", tagId: "11", tagName: "上古" }],
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
                sourceReferences: [
                    {
                        href: "/knowledge/atlas",
                        snippet:
                            "当前展示的是最新已应用图谱版本，可继续查看关联实体、来源与时间线。",
                        sourceId: "1001",
                        sourceTitle: "三才图会",
                        sourceType: "SANCAI_ENTRY",
                        updatedAt: null
                    }
                ],
                timelineItems: [
                    {
                        description: "该实体在图谱中首次被抽取并登记。",
                        href: "/knowledge/atlas",
                        timeLabel: "首次抽取",
                        title: "知识首次进入图谱"
                    }
                ]
            },
            overviewView: null
        });

        const { container, root } = renderPage("/knowledge/atlas?level=detail&entityId=3001");
        await flushQuery();

        expect(container.textContent).toContain("黄帝");
        expect(container.textContent).toContain("实体卷宗");
        expect(container.textContent).toContain("帝系关系");
        expect(container.textContent).toContain("三才图会");
        expect(container.textContent).toContain("相关标签：");
        expect(getKnowledgeAtlas).toHaveBeenCalledWith({
            categoryCode: null,
            entityId: 3001,
            level: "detail"
        });

        act(() => {
            root.unmount();
        });
    });
});
