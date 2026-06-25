export interface KnowledgeHomeStat {
    key: string;
    label: string;
    value: string;
    deltaText: string;
    trend: string;
    icon: string;
}

export interface KnowledgeHomeQuickLink {
    key: string;
    label: string;
    description: string;
    href: string;
    type: string;
}

export interface KnowledgeHomeRecentUpdate {
    title: string;
    subtitle: string;
    summary: string;
    updatedAt: number | null;
    href: string;
    coverImageUrl: string | null;
}

export interface KnowledgeHomeFeatureCollection {
    key: string;
    label: string;
    description: string;
    href: string;
    badgeText: string;
}

export interface KnowledgeHomeResponse {
    heroTitle: string;
    heroSubtitle: string;
    searchPlaceholder: string;
    stats: KnowledgeHomeStat[];
    quickLinks: KnowledgeHomeQuickLink[];
    recentUpdates: KnowledgeHomeRecentUpdate[];
    featureCollections: KnowledgeHomeFeatureCollection[];
}
