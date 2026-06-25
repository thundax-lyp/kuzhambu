import { getJson } from "@/api/http";
import type { KnowledgeAtlasQuery, KnowledgeAtlasResponse } from "./knowledge-atlas-types";

const buildFallback = (query?: KnowledgeAtlasQuery): KnowledgeAtlasResponse => {
    const level = query?.level ?? "overview";
    const availableFilters = {
        entityTypes: ["PERSON", "CREATURE", "RITUAL", "ARTIFACT"],
        knowledgeBases: ["SANCAI_ENTRY", "MING_CUSTOM", "WANGQI_DOC"],
        relationTypes: ["ANCESTOR", "KIN", "RITUAL"],
        tagNames: ["上古", "礼制", "鸟兽"],
        timeRanges: ["30d", "90d", "all"]
    };

    if (level === "category") {
        return {
            availableFilters,
            breadcrumbItems: [
                { href: "/knowledge/atlas?level=overview", label: "图谱总览", level: "overview" },
                {
                    href: `/knowledge/atlas?level=category&categoryCode=${query?.categoryCode ?? "BIRDS"}`,
                    label: "羽族",
                    level: "category"
                }
            ],
            categoryView: {
                categoryCode: query?.categoryCode ?? "BIRDS",
                categoryName: "羽族",
                entityHighlights: [
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
        };
    }

    if (level === "detail") {
        const entityId = query?.entityId ?? 3001;
        return {
            availableFilters,
            breadcrumbItems: [
                { href: "/knowledge/atlas?level=overview", label: "图谱总览", level: "overview" },
                {
                    href: "/knowledge/atlas?level=category&categoryCode=BIRDS",
                    label: "羽族",
                    level: "category"
                },
                {
                    href: `/knowledge/atlas?level=detail&entityId=${entityId}`,
                    label: entityId === 3002 ? "凤" : "黄帝",
                    level: "detail"
                }
            ],
            categoryView: null,
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

    return {
        availableFilters,
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
                },
                {
                    appliedVersionCount: 1,
                    categoryCode: "RITUAL",
                    categoryName: "礼制",
                    entityCount: 4,
                    entryHref: "/knowledge/atlas?level=category&categoryCode=RITUAL",
                    latestVersionNo: 2,
                    relationCount: 3
                }
            ],
            summarySubtitle: "先看门类分布，再进入单门类浏览与单实体详情。",
            summaryTitle: "十四门类知识鸟瞰"
        }
    };
};

export const KNOWLEDGE_ATLAS_FALLBACK = buildFallback();

export const getKnowledgeAtlas = async (query?: KnowledgeAtlasQuery) => {
    try {
        return await getJson<KnowledgeAtlasResponse>("/portal/knowledge/atlas", {
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
