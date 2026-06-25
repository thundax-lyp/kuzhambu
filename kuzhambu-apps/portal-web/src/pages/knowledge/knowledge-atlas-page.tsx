import { useQuery } from "@tanstack/react-query";
import { ArrowRight, Binary, Filter, Orbit, ScrollText } from "lucide-react";
import type { ReactNode } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Card } from "@/components/ui/card";
import * as KnowledgeAtlasService from "./knowledge-atlas-service";

import "./knowledge-atlas-page.css";

const parseAtlasQuery = (searchParams: URLSearchParams) => {
    const level = searchParams.get("level");
    const entityId = searchParams.get("entityId");

    return {
        categoryCode: searchParams.get("categoryCode"),
        entityId: entityId ? Number(entityId) : null,
        level:
            level === "category" || level === "detail" || level === "overview" ? level : "overview"
    } as const;
};

export const KnowledgeAtlasPage = () => {
    const [searchParams] = useSearchParams();
    const atlasQueryParams = parseAtlasQuery(searchParams);
    const atlasQuery = useQuery({
        queryFn: () => KnowledgeAtlasService.getKnowledgeAtlas(atlasQueryParams),
        queryKey: [
            "knowledge-atlas",
            atlasQueryParams.level,
            atlasQueryParams.categoryCode,
            atlasQueryParams.entityId
        ]
    });
    const content = atlasQuery.data ?? KnowledgeAtlasService.KNOWLEDGE_ATLAS_FALLBACK;
    const filterGroups = [
        { label: "知识库", values: content.availableFilters.knowledgeBases },
        { label: "实体类型", values: content.availableFilters.entityTypes },
        { label: "时间范围", values: content.availableFilters.timeRanges }
    ];
    let detailDescription = `${content.detailView?.focusNode.summary}。详情栏会承接实体摘要、来源说明、时间线和相关标签。`;
    if (content.currentLevel === "overview") {
        detailDescription =
            "当前停留在图谱总览层，先从门类密度和版本新鲜度判断下一步要进入哪一卷。";
    } else if (content.currentLevel === "category") {
        detailDescription =
            "当前停留在门类层，可先读版本摘要与关系分组，再选择代表实体进入 detail。";
    }
    let clueItems: ReactNode;
    if (content.currentLevel === "category") {
        clueItems = content.categoryView?.sourceReferences.map((source) => (
            <li key={source.sourceTitle}>
                {source.sourceTitle}：{source.snippet}
            </li>
        ));
    } else if (content.currentLevel === "detail") {
        clueItems = [
            ...(content.detailView?.sourceReferences.map((source) => (
                <li key={source.sourceTitle}>
                    {source.sourceTitle}：{source.snippet}
                </li>
            )) ?? []),
            ...(content.detailView?.timelineItems.map((note) => (
                <li key={note.timeLabel}>
                    {note.timeLabel}：{note.title}
                </li>
            )) ?? []),
            ...(content.detailView?.relatedTags.map((tag) => (
                <li key={tag.tagId}>相关标签：{tag.tagName}</li>
            )) ?? [])
        ];
    } else {
        clueItems = content.breadcrumbItems.map((item) => (
            <li key={item.href}>
                {item.level}：{item.label}
            </li>
        ));
    }

    return (
        <main className="knowledge-atlas-shell">
            <header className="knowledge-atlas-header">
                <div>
                    <p className="knowledge-atlas-kicker">Knowledge Atlas</p>
                    <h1>图谱浏览台</h1>
                    <p>URL 会直接承载总览、门类与实体三层状态，刷新后仍能回到当前浏览层级。</p>
                </div>
                <Link className="knowledge-atlas-back" to="/knowledge">
                    返回知识首页
                    <ArrowRight size={16} />
                </Link>
            </header>

            <nav className="knowledge-atlas-breadcrumb" aria-label="Atlas breadcrumb">
                {content.breadcrumbItems.map((item, index) => (
                    <span key={item.href} className="knowledge-atlas-breadcrumb-item">
                        <Link to={item.href}>{item.label}</Link>
                        {index < content.breadcrumbItems.length - 1 ? (
                            <span className="knowledge-atlas-breadcrumb-separator">/</span>
                        ) : null}
                    </span>
                ))}
            </nav>

            <section className="knowledge-atlas-layout">
                <Card className="knowledge-atlas-filter-panel">
                    <div className="knowledge-atlas-panel-heading">
                        <Filter size={18} />
                        <div>
                            <p>筛选</p>
                            <h2>知识入口条件</h2>
                        </div>
                    </div>

                    {filterGroups.map((group) => (
                        <div key={group.label} className="knowledge-atlas-filter-group">
                            <h3>{group.label}</h3>
                            <div className="knowledge-atlas-chip-list">
                                {group.values.map((value) => (
                                    <span key={value} className="knowledge-atlas-chip">
                                        {value}
                                    </span>
                                ))}
                            </div>
                        </div>
                    ))}
                </Card>

                <Card className="knowledge-atlas-stage">
                    <div className="knowledge-atlas-panel-heading">
                        <Binary size={18} />
                        <div>
                            <p>浏览</p>
                            <h2>{content.currentLevel === "overview" ? "总览舞台" : "关系舞台"}</h2>
                        </div>
                    </div>

                    {content.currentLevel === "overview" && content.overviewView ? (
                        <div className="knowledge-atlas-overview-stage">
                            <div className="knowledge-atlas-overview-hero">
                                <span>总览卷宗</span>
                                <strong>{content.overviewView.summaryTitle}</strong>
                                <small>{content.overviewView.summarySubtitle}</small>
                            </div>
                            <div className="knowledge-atlas-overview-grid">
                                {content.overviewView.categoryCards.map((card) => (
                                    <article
                                        key={card.categoryCode}
                                        className="knowledge-atlas-overview-card"
                                    >
                                        <div className="knowledge-atlas-overview-card-head">
                                            <Orbit size={18} />
                                            <span>{card.categoryCode}</span>
                                        </div>
                                        <h3>{card.categoryName}</h3>
                                        <p>
                                            实体 {card.entityCount} · 关系 {card.relationCount}
                                        </p>
                                        <dl className="knowledge-atlas-overview-meta">
                                            <div>
                                                <dt>应用版本</dt>
                                                <dd>{card.appliedVersionCount}</dd>
                                            </div>
                                            <div>
                                                <dt>最新批次</dt>
                                                <dd>V{card.latestVersionNo}</dd>
                                            </div>
                                        </dl>
                                        <Link to={card.entryHref}>进入门类</Link>
                                    </article>
                                ))}
                            </div>
                        </div>
                    ) : null}

                    {content.currentLevel === "category" && content.categoryView ? (
                        <div className="knowledge-atlas-category-stage">
                            <div className="knowledge-atlas-category-hero">
                                <span>{content.categoryView.categoryCode}</span>
                                <strong>{content.categoryView.categoryName}</strong>
                                <small>
                                    版本 {content.categoryView.latestVersionNo} · 门类层浏览
                                </small>
                            </div>

                            <div className="knowledge-atlas-category-meta">
                                <div>
                                    <dt>版本编号</dt>
                                    <dd>{content.categoryView.latestVersionId}</dd>
                                </div>
                                <div>
                                    <dt>代表实体</dt>
                                    <dd>{content.categoryView.entityHighlights.length}</dd>
                                </div>
                                <div>
                                    <dt>关系分组</dt>
                                    <dd>{content.categoryView.relationGroups.length}</dd>
                                </div>
                            </div>

                            <div className="knowledge-atlas-category-grid">
                                {content.categoryView.entityHighlights.map((entity) => (
                                    <article
                                        key={entity.entityId}
                                        className="knowledge-atlas-category-card"
                                    >
                                        <div className="knowledge-atlas-overview-card-head">
                                            <Orbit size={18} />
                                            <span>{entity.entityType}</span>
                                        </div>
                                        <h3>{entity.entityName}</h3>
                                        <p>{entity.confirmationStatus}</p>
                                        <Link to={entity.entryHref}>进入详情</Link>
                                    </article>
                                ))}
                            </div>

                            {content.categoryView.relationGroups.map((group) => (
                                <article key={group.groupKey} className="knowledge-atlas-band">
                                    <Orbit size={18} />
                                    <div>
                                        <h3>{group.groupLabel}</h3>
                                        <p>
                                            {group.relations[0]?.sourceLabel} →{" "}
                                            {group.relations[0]?.targetLabel} ·{" "}
                                            {group.relations[0]?.relationLabel}
                                        </p>
                                    </div>
                                </article>
                            ))}
                        </div>
                    ) : null}

                    {content.currentLevel === "detail" && content.detailView ? (
                        <div className="knowledge-atlas-detail-stage">
                            <div className="knowledge-atlas-detail-hero">
                                <span>实体卷宗</span>
                                <strong>{content.detailView.focusNode.title}</strong>
                                <small>
                                    {content.detailView.focusNode.type} ·{" "}
                                    {content.detailView.focusNode.summary} ·{" "}
                                    {content.detailView.focusNode.status}
                                </small>
                            </div>

                            <div className="knowledge-atlas-detail-grid">
                                {content.detailView.relationGroups.map((band) => (
                                    <article
                                        key={band.groupKey}
                                        className="knowledge-atlas-detail-card"
                                    >
                                        <div className="knowledge-atlas-overview-card-head">
                                            <Orbit size={18} />
                                            <span>{band.groupKey}</span>
                                        </div>
                                        <h3>{band.groupLabel}</h3>
                                        <p>
                                            {band.relations[0]?.sourceLabel} →{" "}
                                            {band.relations[0]?.targetLabel} ·{" "}
                                            {band.relations[0]?.relationLabel}
                                        </p>
                                    </article>
                                ))}
                            </div>
                        </div>
                    ) : null}
                </Card>

                <Card className="knowledge-atlas-detail-panel">
                    <div className="knowledge-atlas-panel-heading">
                        <ScrollText size={18} />
                        <div>
                            <p>详情</p>
                            <h2>{content.currentLevel === "overview" ? "层级说明" : "焦点说明"}</h2>
                        </div>
                    </div>

                    <section className="knowledge-atlas-detail-block">
                        <h3>{content.breadcrumbItems.map((item) => item.label).join(" / ")}</h3>
                        <p>{detailDescription}</p>
                    </section>

                    <section className="knowledge-atlas-detail-block">
                        <h3>当前线索</h3>
                        <ul>{clueItems}</ul>
                    </section>
                </Card>
            </section>
        </main>
    );
};
