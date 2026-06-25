import { getJson } from "@/api/http";
import type { KnowledgeHomeResponse } from "./knowledge-home-types";

export const KNOWLEDGE_HOME_FALLBACK: KnowledgeHomeResponse = {
    featureCollections: [
        {
            badgeText: "版本视图",
            description: "快速进入最近一次已应用的知识快照。",
            href: "/knowledge/atlas",
            key: "latest-atlas",
            label: "最新图谱版本"
        },
        {
            badgeText: "关系阅读",
            description: "从人物、器物、礼制等实体切入浏览关联。",
            href: "/knowledge/atlas",
            key: "entity-gallery",
            label: "实体总览"
        }
    ],
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
            description: "查看确认率、来源构成与当前待处理事项。",
            href: "/knowledge/quality",
            key: "quality",
            label: "质量总览",
            type: "quality"
        }
    ],
    recentUpdates: [
        {
            coverImageUrl: null,
            href: "/knowledge/atlas",
            subtitle: "知识快照最近整理",
            summary: "整理上古人物关系链，补齐黄帝相关亲缘与时代标签。",
            title: "三才图会 · 帝系知识快照",
            updatedAt: null
        },
        {
            coverImageUrl: null,
            href: "/knowledge/quality",
            subtitle: "治理结果同步",
            summary: "围绕节令和礼仪，新增一组制度关联与来源注记。",
            title: "明代习俗 · 岁时礼制增补",
            updatedAt: null
        }
    ],
    searchPlaceholder: "人物 · 器物 · 礼制 · 典故 · 版本",
    stats: [
        {
            deltaText: "人物、器物、礼制与典故共览",
            icon: "seal",
            key: "entity-count",
            label: "图谱实体",
            trend: "steady",
            value: "12,480"
        },
        {
            deltaText: "从亲缘到制度脉络持续延展",
            icon: "constellation",
            key: "relation-count",
            label: "关系弧线",
            trend: "up",
            value: "3,126"
        },
        {
            deltaText: "按版本追溯知识整理过程",
            icon: "scroll",
            key: "graph-version-count",
            label: "已应用版本",
            trend: "steady",
            value: "268"
        }
    ]
};

export const getKnowledgeHome = async () => {
    try {
        return await getJson<KnowledgeHomeResponse>("/portal/knowledge/home");
    } catch {
        return KNOWLEDGE_HOME_FALLBACK;
    }
};
