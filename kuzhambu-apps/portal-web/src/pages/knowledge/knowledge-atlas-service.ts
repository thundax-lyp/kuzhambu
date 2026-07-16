import { postJson } from "@/api/http";
import type {
    KnowledgeAtlasCanvasView,
    KnowledgeAtlasOverviewCategoryCard,
    KnowledgeAtlasQuery,
    KnowledgeAtlasResponse
} from "./knowledge-atlas-types";

const SANCAI_CATEGORY_SLOTS = [
    { code: "ASTRONOMY", name: "天文" },
    { code: "GEOGRAPHY", name: "地理" },
    { code: "PEOPLE", name: "人物" },
    { code: "SEASONS", name: "时令" },
    { code: "PALACES", name: "宫室" },
    { code: "TOOLS", name: "器用" },
    { code: "BODY", name: "身体" },
    { code: "CLOTHING", name: "衣服" },
    { code: "AFFAIRS", name: "人事" },
    { code: "RITUALS", name: "仪制" },
    { code: "TREASURES", name: "珍宝" },
    { code: "LITERATURE", name: "文史" },
    { code: "ANIMALS", name: "鸟兽" },
    { code: "PLANTS", name: "草木" }
];

const availableFilters = {
    entityTypes: ["PERSON", "CREATURE", "RITUAL", "ARTIFACT"],
    knowledgeBases: ["SANCAI_ENTRY", "MING_CUSTOM", "WANGQI_DOC"],
    relationTypes: ["ANCESTOR", "KIN", "RITUAL"],
    tagNames: ["上古", "礼制", "鸟兽"],
    timeRanges: ["30d", "90d", "all"]
};

const overviewCards = (): KnowledgeAtlasOverviewCategoryCard[] =>
    SANCAI_CATEGORY_SLOTS.map((slot) => {
        if (slot.code === "ANIMALS") {
            return {
                appliedVersionCount: 2,
                categoryCode: slot.code,
                categoryName: slot.name,
                entityCount: 2,
                entryHref: "/knowledge/atlas?level=category&categoryCode=ANIMALS",
                latestVersionNo: 3,
                relationCount: 1
            };
        }
        return {
            appliedVersionCount: 0,
            categoryCode: slot.code,
            categoryName: slot.name,
            entityCount: 0,
            entryHref: `/knowledge/atlas?level=category&categoryCode=${slot.code}`,
            latestVersionNo: null,
            relationCount: 0
        };
    });

const buildOverviewCanvas = (
    cards: KnowledgeAtlasOverviewCategoryCard[]
): KnowledgeAtlasCanvasView => ({
    description: "固定展示三才图会十四个正式门类，空门类保留可进入空态。",
    edges: cards.map((card) => ({
        dashed: card.appliedVersionCount === 0,
        id: `root:sancai->category:${card.categoryCode}`,
        label: card.appliedVersionCount === 0 ? "空位" : "门类",
        relationType: "CATEGORY",
        source: "root:sancai",
        target: `category:${card.categoryCode}`,
        weight: card.appliedVersionCount === 0 ? 0.2 : 1
    })),
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
            metricValue: SANCAI_CATEGORY_SLOTS.length,
            status: "active",
            subtitle: "固定十四门类",
            weight: 1,
            x: 0,
            y: 0
        },
        ...cards.map((card, index) => {
            const angle = (Math.PI * 2 * index) / cards.length - Math.PI / 2;
            const empty = card.appliedVersionCount === 0;
            return {
                categoryCode: card.categoryCode,
                entityId: null,
                href: card.entryHref,
                id: `category:${card.categoryCode}`,
                kind: "category",
                label: card.categoryName,
                metricLabel: "实体",
                metricValue: card.entityCount,
                status: empty ? "empty" : "active",
                subtitle: empty ? "等待图谱版本" : `版本 ${card.latestVersionNo}`,
                weight: card.entityCount,
                x: Math.cos(angle) * 280,
                y: Math.sin(angle) * 280
            };
        })
    ],
    title: "十四门类知识图谱"
});

const buildCategoryCanvas = (
    categoryCode: string,
    categoryName: string
): KnowledgeAtlasCanvasView => ({
    description: "展示当前门类最新已应用图谱版本中的实体与关系。",
    edges: [
        {
            dashed: false,
            id: "category:ANIMALS->entity:3001",
            label: "包含",
            relationType: "CATEGORY_ENTITY",
            source: "category:ANIMALS",
            target: "entity:3001",
            weight: 0.95
        },
        {
            dashed: false,
            id: "category:ANIMALS->entity:3002",
            label: "包含",
            relationType: "CATEGORY_ENTITY",
            source: "category:ANIMALS",
            target: "entity:3002",
            weight: 0.95
        },
        {
            dashed: false,
            id: "relation:bird:luan-feng",
            label: "KIN",
            relationType: "KIN",
            source: "entity:3001",
            target: "entity:3002",
            weight: 0.92
        }
    ],
    empty: false,
    emptyDescription: null,
    emptyTitle: null,
    focusNodeId: `category:${categoryCode}`,
    mode: "category",
    nodes: [
        {
            categoryCode,
            entityId: null,
            href: `/knowledge/atlas?level=category&categoryCode=${categoryCode}`,
            id: `category:${categoryCode}`,
            kind: "category",
            label: categoryName,
            metricLabel: "实体",
            metricValue: 2,
            status: "active",
            subtitle: "版本 3",
            weight: 2,
            x: 0,
            y: 0
        },
        {
            categoryCode: null,
            entityId: 3001,
            href: "/knowledge/atlas?level=detail&entityId=3001",
            id: "entity:3001",
            kind: "entity",
            label: "鸾",
            metricLabel: "置信",
            metricValue: 95,
            status: "CONFIRMED",
            subtitle: "CREATURE",
            weight: 0.95,
            x: null,
            y: null
        },
        {
            categoryCode: null,
            entityId: 3002,
            href: "/knowledge/atlas?level=detail&entityId=3002",
            id: "entity:3002",
            kind: "entity",
            label: "凤",
            metricLabel: "置信",
            metricValue: 95,
            status: "CONFIRMED",
            subtitle: "CREATURE",
            weight: 0.95,
            x: null,
            y: null
        }
    ],
    title: `${categoryName}知识图谱`
});

const buildEmptyCategoryCanvas = (
    categoryCode: string,
    categoryName: string
): KnowledgeAtlasCanvasView => ({
    description: "该门类保留固定空位，等待图谱版本应用后展示实体关系。",
    edges: [],
    empty: true,
    emptyDescription: "当前门类尚无已应用图谱版本，Portal 仅展示固定 14 门类空位。",
    emptyTitle: `暂无${categoryName}图谱`,
    focusNodeId: `category:${categoryCode}`,
    mode: "category",
    nodes: [
        {
            categoryCode,
            entityId: null,
            href: `/knowledge/atlas?level=category&categoryCode=${categoryCode}`,
            id: `category:${categoryCode}`,
            kind: "category",
            label: categoryName,
            metricLabel: "实体",
            metricValue: 0,
            status: "empty",
            subtitle: "固定十四门类空位",
            weight: 0,
            x: 0,
            y: 0
        }
    ],
    title: `${categoryName}知识图谱`
});

const buildDetailCanvas = (entityId: number): KnowledgeAtlasCanvasView => {
    const focusTitle = entityId === 3002 ? "凤" : "黄帝";
    const relatedTitle = entityId === 3002 ? "鸾" : "少典";
    return {
        description: "展示焦点实体与直接相邻关系。",
        edges: [
            {
                dashed: false,
                id: "relation:detail",
                label: entityId === 3002 ? "KIN" : "ANCESTOR",
                relationType: entityId === 3002 ? "KIN" : "ANCESTOR",
                source: `entity:${entityId}`,
                target: "entity-key:related",
                weight: 0.95
            }
        ],
        empty: false,
        emptyDescription: null,
        emptyTitle: null,
        focusNodeId: `entity:${entityId}`,
        mode: "detail",
        nodes: [
            {
                categoryCode: null,
                entityId,
                href: `/knowledge/atlas?level=detail&entityId=${entityId}`,
                id: `entity:${entityId}`,
                kind: "entity",
                label: focusTitle,
                metricLabel: "置信",
                metricValue: 95,
                status: "CONFIRMED",
                subtitle: entityId === 3002 ? "CREATURE" : "PERSON",
                weight: 0.95,
                x: null,
                y: null
            },
            {
                categoryCode: null,
                entityId: null,
                href: null,
                id: "entity-key:related",
                kind: "entity",
                label: relatedTitle,
                metricLabel: "置信",
                metricValue: null,
                status: "RELATED",
                subtitle: "关系端点",
                weight: 0.7,
                x: null,
                y: null
            }
        ],
        title: `${focusTitle}关系图谱`
    };
};

const buildFallback = (query?: KnowledgeAtlasQuery): KnowledgeAtlasResponse => {
    const level = query?.level ?? "overview";

    if (level === "category") {
        const categoryCode = query?.categoryCode ?? "ANIMALS";
        const categoryName =
            SANCAI_CATEGORY_SLOTS.find((slot) => slot.code === categoryCode)?.name ?? "鸟兽";
        const populated = categoryCode === "ANIMALS";
        return {
            availableFilters,
            breadcrumbItems: [
                { href: "/knowledge/atlas?level=overview", label: "图谱总览", level: "overview" },
                {
                    href: `/knowledge/atlas?level=category&categoryCode=${categoryCode}`,
                    label: categoryName,
                    level: "category"
                }
            ],
            categoryView: {
                categoryCode,
                categoryName,
                entityHighlights: populated
                    ? [
                          {
                              confirmationStatus: "CONFIRMED",
                              entityId: "3001",
                              entityName: "鸾",
                              entityType: "CREATURE",
                              entryHref: "/knowledge/atlas?level=detail&entityId=3001"
                          },
                          {
                              confirmationStatus: "CONFIRMED",
                              entityId: "3002",
                              entityName: "凤",
                              entityType: "CREATURE",
                              entryHref: "/knowledge/atlas?level=detail&entityId=3002"
                          }
                      ]
                    : [],
                latestVersionId: populated ? 71 : null,
                latestVersionNo: populated ? 3 : null,
                relationGroups: populated
                    ? [
                          {
                              groupKey: "KIN",
                              groupLabel: "鸟兽关联",
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
                      ]
                    : [],
                sourceReferences: populated
                    ? [
                          {
                              href: "/knowledge/atlas",
                              snippet: "当前门类来自最近一次已应用图谱版本，可继续进入单实体详情。",
                              sourceId: "1001",
                              sourceTitle: "鸟兽",
                              sourceType: "SANCAI_ENTRY",
                              updatedAt: null
                          }
                      ]
                    : []
            },
            canvasView: populated
                ? buildCategoryCanvas(categoryCode, categoryName)
                : buildEmptyCategoryCanvas(categoryCode, categoryName),
            currentLevel: "category",
            detailView: null,
            overviewView: null
        };
    }

    if (level === "detail") {
        const entityId = query?.entityId ?? 3001;
        return {
            availableFilters,
            breadcrumbItems: [
                { href: "/knowledge/atlas?level=overview", label: "图谱总览", level: "overview" },
                {
                    href: "/knowledge/atlas?level=category&categoryCode=ANIMALS",
                    label: "鸟兽",
                    level: "category"
                },
                {
                    href: `/knowledge/atlas?level=detail&entityId=${entityId}`,
                    label: entityId === 3002 ? "凤" : "黄帝",
                    level: "detail"
                }
            ],
            categoryView: null,
            canvasView: buildDetailCanvas(entityId),
            currentLevel: "detail",
            detailView: {
                focusNode: {
                    confidence: 0.95,
                    coverImageUrl: null,
                    id: String(entityId),
                    status: "CONFIRMED",
                    summary: entityId === 3002 ? "瑞鸟" : "上古始祖",
                    title: entityId === 3002 ? "凤" : "黄帝",
                    type: entityId === 3002 ? "CREATURE" : "PERSON"
                },
                relatedTags: [
                    { score: 0.92, tagCategory: "时代", tagId: "11", tagName: "上古" },
                    { score: 0.87, tagCategory: "主题", tagId: "12", tagName: "帝系" }
                ],
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
                    },
                    {
                        description: "该实体在最近一次图谱应用中被重新刷新。",
                        href: "/knowledge/atlas",
                        timeLabel: "最近抽取",
                        title: "知识最近一次刷新"
                    }
                ]
            },
            overviewView: null
        };
    }

    const cards = overviewCards();
    return {
        availableFilters,
        breadcrumbItems: [
            { href: "/knowledge/atlas?level=overview", label: "图谱总览", level: "overview" }
        ],
        categoryView: null,
        canvasView: buildOverviewCanvas(cards),
        currentLevel: "overview",
        detailView: null,
        overviewView: {
            categoryCards: cards,
            summarySubtitle: "先看门类分布，再进入单门类浏览与单实体详情。",
            summaryTitle: "十四门类知识鸟瞰"
        }
    };
};

export const KNOWLEDGE_ATLAS_FALLBACK = buildFallback();

export const getKnowledgeAtlas = async (query?: KnowledgeAtlasQuery) => {
    try {
        return await postJson<KnowledgeAtlasResponse>("/portal/knowledge/atlas/get", {
            categoryCode: query?.categoryCode,
            entityId: query?.entityId,
            keyword: query?.keyword,
            knowledgeBase: query?.knowledgeBase,
            level: query?.level,
            tag: query?.tag,
            timeRange: query?.timeRange
        });
    } catch {
        return buildFallback(query);
    }
};
