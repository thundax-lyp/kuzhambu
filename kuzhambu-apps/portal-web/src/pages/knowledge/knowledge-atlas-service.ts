import { getJson } from "@/api/http";
import type { KnowledgeAtlasResponse } from "./knowledge-atlas-types";

export const KNOWLEDGE_ATLAS_FALLBACK: KnowledgeAtlasResponse = {
    availableFilters: {
        entityTypes: ["人物", "器物", "礼制", "典故"],
        knowledgeBases: ["三才图会", "明代习俗", "王圻文档"],
        relationTypes: ["帝系关系", "礼制关联", "来源脉络"],
        tagNames: ["上古", "礼器", "岁时"],
        timeRanges: ["最近 30 天", "最近 90 天", "全部版本"]
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
        },
        {
            groupKey: "RITUAL",
            groupLabel: "礼制关联",
            relations: [
                {
                    relationLabel: "RITUAL",
                    relationType: "RITUAL",
                    sourceId: "person:huangdi",
                    sourceLabel: "黄帝",
                    targetId: "ritual:suburban",
                    targetLabel: "郊祀",
                    weight: 0.82
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
        },
        {
            description: "该实体已经过人工确认，可用于更稳定的展示。",
            href: "/knowledge/atlas",
            timeLabel: "人工确认",
            title: "知识已完成人工确认"
        }
    ]
};

export const getKnowledgeAtlas = async () => {
    try {
        return await getJson<KnowledgeAtlasResponse>("/portal/knowledge/atlas");
    } catch {
        return KNOWLEDGE_ATLAS_FALLBACK;
    }
};
