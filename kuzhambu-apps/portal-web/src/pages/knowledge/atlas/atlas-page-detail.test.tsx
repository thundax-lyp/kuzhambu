import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act } from "react";
import type { ReactNode } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KnowledgeAtlasPage } from "./atlas-page";

const { getKnowledgeAtlas } = vi.hoisted(() => ({
    getKnowledgeAtlas: vi.fn()
}));

/* eslint-disable @typescript-eslint/naming-convention */
vi.mock("@xyflow/react", () => ({
    Background: () => <div data-testid="graph-background" />,
    BackgroundVariant: { Dots: "dots" },
    Controls: () => <div data-testid="graph-controls" />,
    MarkerType: { ArrowClosed: "arrowclosed" },
    MiniMap: () => <div data-testid="graph-minimap" />,
    ReactFlow: ({
        children,
        nodes,
        onNodeClick
    }: {
        children: ReactNode;
        nodes: { data: { href?: string | null; label?: string }; id: string }[];
        onNodeClick?: (
            event: unknown,
            node: { data: { href?: string | null; label?: string }; id: string }
        ) => void;
    }) => (
        <div data-testid="react-flow">
            {nodes.map((node) => (
                <button key={node.id} type="button" onClick={() => onNodeClick?.({}, node)}>
                    {node.data.label}
                </button>
            ))}
            {children}
        </div>
    )
}));
/* eslint-enable @typescript-eslint/naming-convention */

vi.mock("./atlas-service", () => ({
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
        canvasView: {
            description: "固定展示三才图会十四个正式门类，空门类保留可进入空态。",
            edges: [],
            empty: false,
            emptyDescription: null,
            emptyTitle: null,
            focusNodeId: "root:sancai",
            mode: "overview",
            nodes: [
                {
                    categoryCode: null,
                    entityId: null,
                    href: "/knowledge/atlas?level=overview",
                    id: "root:sancai",
                    kind: "root",
                    label: "三才图会",
                    metricLabel: "门类",
                    metricValue: 14,
                    status: "active",
                    subtitle: "固定十四门类",
                    weight: 1,
                    x: 0,
                    y: 0
                }
            ],
            title: "十四门类知识图谱"
        },
        currentLevel: "overview",
        detailView: null,
        overviewView: {
            categoryCards: [
                {
                    appliedVersionCount: 2,
                    categoryCode: "ANIMALS",
                    categoryName: "鸟兽",
                    entityCount: 2,
                    entryHref: "/knowledge/atlas?level=category&categoryCode=ANIMALS",
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

const mockCanvasView = (
    mode: "overview" | "category" | "detail",
    title: string,
    label: string
) => ({
    description: "图谱画布说明",
    edges: [],
    empty: false,
    emptyDescription: null,
    emptyTitle: null,
    focusNodeId: `${mode}:focus`,
    mode,
    nodes: [
        {
            categoryCode: mode === "category" ? "ANIMALS" : null,
            entityId: mode === "detail" ? 3001 : null,
            href: mode === "detail" ? "/knowledge/atlas?level=detail&entityId=3001" : null,
            id: `${mode}:focus`,
            kind: mode === "overview" ? "root" : mode,
            label,
            metricLabel: "实体",
            metricValue: 1,
            status: "active",
            subtitle: "mock",
            weight: 1,
            x: 0,
            y: 0
        }
    ],
    title
});

describe("KnowledgeAtlasPage", () => {
    afterEach(() => {
        document.body.innerHTML = "";
        getKnowledgeAtlas.mockReset();
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
                    href: "/knowledge/atlas?level=category&categoryCode=ANIMALS",
                    label: "鸟兽",
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
            canvasView: mockCanvasView("detail", "黄帝关系图谱", "黄帝"),
            overviewView: null
        });

        const { container, root } = renderPage("/knowledge/atlas?level=detail&entityId=3001");
        await flushQuery();

        expect(container.textContent).toContain("黄帝");
        expect(container.textContent).toContain("黄帝关系图谱");
        expect(container.textContent).toContain("实体卷宗");
        expect(container.textContent).toContain("帝系关系");
        expect(container.textContent).toContain("三才图会");
        expect(container.textContent).toContain("相关标签：");
        const detailLinks = Array.from(container.querySelectorAll("a")).map((link) =>
            link.getAttribute("href")
        );
        expect(detailLinks).toContain("/knowledge/atlas?level=overview");
        expect(detailLinks).toContain("/knowledge/atlas?level=category&categoryCode=ANIMALS");
        expect(detailLinks).toContain("/knowledge/atlas?level=detail&entityId=3001");
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
