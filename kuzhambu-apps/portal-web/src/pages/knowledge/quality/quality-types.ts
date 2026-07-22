export interface KnowledgeQualityStat {
    key: string;
    label: string;
    value: string;
    unit: string;
    deltaText: string;
    statusTone: string;
}

export interface KnowledgeQualityTrendPoint {
    label: string;
    value: number;
}

export interface KnowledgeQualityTrendSeries {
    seriesKey: string;
    seriesLabel: string;
    points: KnowledgeQualityTrendPoint[];
}

export interface KnowledgeQualitySourceBreakdown {
    sourceKey: string;
    sourceLabel: string;
    value: number;
    description: string;
}

export interface KnowledgeQualityFocusIssue {
    title: string;
    summary: string;
    severity: string;
    href: string;
}

export interface KnowledgeQualitySourceDetail {
    sourceType: string;
    sourceTitle: string;
    updatedAt: number | null;
    status: string;
    href: string;
}

export interface KnowledgeQualityResponse {
    qualityStats: KnowledgeQualityStat[];
    trendSeries: KnowledgeQualityTrendSeries[];
    sourceBreakdowns: KnowledgeQualitySourceBreakdown[];
    focusIssues: KnowledgeQualityFocusIssue[];
    sourceDetails: KnowledgeQualitySourceDetail[];
}
