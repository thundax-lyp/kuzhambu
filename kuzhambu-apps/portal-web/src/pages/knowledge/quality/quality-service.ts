import { postJson } from "@/api/http";
import type { KnowledgeQualityResponse } from "./quality-types";

export const KNOWLEDGE_QUALITY_FALLBACK: KnowledgeQualityResponse = {
    focusIssues: [
        {
            href: "/knowledge/atlas",
            severity: "warning",
            summary: "帝系关系仍有一批待确认记录，建议优先治理高频人物关系。",
            title: "帝系关系仍需补齐确认"
        },
        {
            href: "/knowledge/atlas",
            severity: "notice",
            summary: "礼制关联覆盖率仍在提升中，近期适合补齐仪礼和节令脉络。",
            title: "礼制关联仍在持续扩充"
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
        },
        {
            deltaText: "关键关系链当前确认水平",
            key: "relation-confirmed-rate",
            label: "关系确认率",
            statusTone: "watch",
            unit: "%",
            value: "76"
        },
        {
            deltaText: "仍需治理确认的事项",
            key: "draft-task-count",
            label: "待处理任务",
            statusTone: "attention",
            unit: "项",
            value: "18"
        }
    ],
    sourceBreakdowns: [
        {
            description: "已应用版本数保持领先，适合作为当前图谱展示主来源。",
            sourceKey: "sancai",
            sourceLabel: "三才图会",
            value: 12
        },
        {
            description: "制度与岁时主题近期补录活跃，正在提升覆盖宽度。",
            sourceKey: "ming-customs",
            sourceLabel: "明代习俗",
            value: 7
        }
    ],
    sourceDetails: [
        {
            href: "/knowledge/atlas",
            sourceTitle: "三才图会",
            sourceType: "SANCAI_ENTRY",
            status: "APPLIED",
            updatedAt: null
        },
        {
            href: "/knowledge/atlas",
            sourceTitle: "明代习俗",
            sourceType: "MING_CUSTOM",
            status: "APPLIED",
            updatedAt: null
        }
    ],
    trendSeries: [
        {
            points: [
                { label: "4月", value: 24 },
                { label: "5月", value: 29 },
                { label: "6月", value: 34 }
            ],
            seriesKey: "new-tags",
            seriesLabel: "近三月新增标签"
        },
        {
            points: [
                { label: "4月", value: 8 },
                { label: "5月", value: 10 },
                { label: "6月", value: 12 }
            ],
            seriesKey: "applied-versions",
            seriesLabel: "近三月已应用版本"
        }
    ]
};

export const getKnowledgeQuality = async () => {
    try {
        return await postJson<KnowledgeQualityResponse>("/portal/knowledge/quality/get", {});
    } catch {
        return KNOWLEDGE_QUALITY_FALLBACK;
    }
};
