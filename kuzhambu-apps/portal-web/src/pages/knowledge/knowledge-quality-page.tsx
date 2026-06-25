import { useQuery } from "@tanstack/react-query";
import { Activity, AlertTriangle, ArrowRight, GalleryVerticalEnd, ShieldCheck } from "lucide-react";
import { Link } from "react-router-dom";
import { Card } from "@/components/ui/card";
import { KNOWLEDGE_QUALITY_FALLBACK, getKnowledgeQuality } from "./knowledge-quality-service";

import "./knowledge-quality-page.css";

export const KnowledgeQualityPage = () => {
    const { data = KNOWLEDGE_QUALITY_FALLBACK } = useQuery({
        queryFn: getKnowledgeQuality,
        queryKey: ["knowledge-quality"]
    });

    return (
        <main className="knowledge-quality-shell">
            <header className="knowledge-quality-header">
                <div>
                    <p className="knowledge-quality-kicker">Knowledge Quality</p>
                    <h1>质量总览台</h1>
                    <p>
                        以阅读型摘要理解当前知识状态，不做后台治理台那种重表格信息密度，
                        而是强调确认率、趋势、问题与来源脉络。
                    </p>
                </div>
                <Link className="knowledge-quality-back" to="/knowledge">
                    返回知识首页
                    <ArrowRight size={16} />
                </Link>
            </header>

            <section className="knowledge-quality-stat-grid" aria-label="质量概览">
                {data.qualityStats.map((card) => (
                    <Card key={card.label} className="knowledge-quality-stat-card">
                        <ShieldCheck size={18} />
                        <strong>
                            {card.value}
                            {card.unit}
                        </strong>
                        <h2>{card.label}</h2>
                        <p>{card.deltaText}</p>
                    </Card>
                ))}
            </section>

            <section className="knowledge-quality-layout">
                <Card className="knowledge-quality-trend-panel">
                    <div className="knowledge-quality-panel-heading">
                        <Activity size={18} />
                        <div>
                            <p>趋势</p>
                            <h2>近期变化</h2>
                        </div>
                    </div>
                    <div className="knowledge-quality-trend-grid">
                        {data.trendSeries.map((card) => (
                            <article key={card.seriesKey} className="knowledge-quality-trend-card">
                                <span>{card.seriesLabel}</span>
                                <strong>{card.points.at(-1)?.value ?? 0}</strong>
                            </article>
                        ))}
                    </div>
                </Card>

                <Card className="knowledge-quality-issue-panel">
                    <div className="knowledge-quality-panel-heading">
                        <AlertTriangle size={18} />
                        <div>
                            <p>关注事项</p>
                            <h2>建议优先处理</h2>
                        </div>
                    </div>
                    <div className="knowledge-quality-issue-list">
                        {data.focusIssues.map((issue) => (
                            <article key={issue.title} className="knowledge-quality-issue-card">
                                <strong>{issue.title}</strong>
                                <p>{issue.summary}</p>
                            </article>
                        ))}
                    </div>
                </Card>

                <Card className="knowledge-quality-source-panel">
                    <div className="knowledge-quality-panel-heading">
                        <GalleryVerticalEnd size={18} />
                        <div>
                            <p>来源明细</p>
                            <h2>最近来源快照</h2>
                        </div>
                    </div>
                    <div className="knowledge-quality-source-list">
                        {data.sourceDetails.map((row) => (
                            <div key={row.sourceTitle} className="knowledge-quality-source-row">
                                <div>
                                    <strong>{row.sourceTitle}</strong>
                                    <span>
                                        {row.updatedAt == null ? "最近同步" : `${row.updatedAt}`}
                                    </span>
                                </div>
                                <em>{row.status}</em>
                            </div>
                        ))}
                    </div>
                </Card>
            </section>
        </main>
    );
};
