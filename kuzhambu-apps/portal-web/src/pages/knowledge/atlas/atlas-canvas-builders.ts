import type { KnowledgeAtlasCanvasView, KnowledgeAtlasOverviewCategoryCard } from "./atlas-types";

export const SANCAI_CATEGORY_SLOTS = [
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

export const overviewCards = (): KnowledgeAtlasOverviewCategoryCard[] =>
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

export const buildOverviewCanvas = (
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

export const buildCategoryCanvas = (
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

export const buildEmptyCategoryCanvas = (
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

export const buildDetailCanvas = (entityId: number): KnowledgeAtlasCanvasView => {
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
