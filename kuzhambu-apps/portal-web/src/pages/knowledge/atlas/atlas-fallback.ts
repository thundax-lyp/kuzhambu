import {
    SANCAI_CATEGORY_SLOTS,
    buildCategoryCanvas,
    buildDetailCanvas,
    buildEmptyCategoryCanvas,
    buildOverviewCanvas,
    overviewCards
} from "./atlas-canvas-builders";
import type { KnowledgeAtlasQuery, KnowledgeAtlasResponse } from "./atlas-types";

const availableFilters = {
    entityTypes: ["PERSON", "CREATURE", "RITUAL", "ARTIFACT"],
    knowledgeBases: ["SANCAI_ENTRY", "MING_CUSTOM", "WANGQI_DOC"],
    relationTypes: ["ANCESTOR", "KIN", "RITUAL"],
    tagNames: ["上古", "礼制", "鸟兽"],
    timeRanges: ["30d", "90d", "all"]
};

export const buildKnowledgeAtlasFallback = (
    query?: KnowledgeAtlasQuery
): KnowledgeAtlasResponse => {
    const level = query?.level ?? "overview";

    if (level === "category") {
        return buildCategoryFallback(query);
    }

    if (level === "detail") {
        return buildDetailFallback(query);
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

export const KNOWLEDGE_ATLAS_FALLBACK = buildKnowledgeAtlasFallback();

const buildCategoryFallback = (query?: KnowledgeAtlasQuery): KnowledgeAtlasResponse => {
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
};

const buildDetailFallback = (query?: KnowledgeAtlasQuery): KnowledgeAtlasResponse => {
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
                    snippet: "当前展示的是最新已应用图谱版本，可继续查看关联实体、来源与时间线。",
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
};
