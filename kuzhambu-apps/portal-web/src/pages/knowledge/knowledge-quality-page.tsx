import { Activity, AlertTriangle, ArrowRight, GalleryVerticalEnd, ShieldCheck } from "lucide-react";
import { Link } from "react-router-dom";
import { Card } from "@/components/ui/card";

import "./knowledge-quality-page.css";

const statCards = [
    { label: "实体确认率", note: "人工确认后的核心实体占比", value: "82%" },
    { label: "关系确认率", note: "关键关系链当前确认水平", value: "76%" },
    { label: "待处理任务", note: "仍需治理确认的事项", value: "18" }
];

const trendCards = [
    { label: "近三月新增标签", value: "34" },
    { label: "近三月已应用版本", value: "12" }
];

const issueCards = [
    "帝系关系仍有一批待确认记录，建议优先治理高频人物关系。",
    "礼制关联覆盖率仍在提升中，近期适合补齐仪礼和节令脉络。"
];

const sourceRows = [
    { source: "三才图会", status: "APPLIED", updatedAt: "今日" },
    { source: "明代习俗", status: "APPLIED", updatedAt: "两日前" }
];

export const KnowledgeQualityPage = () => {
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
                {statCards.map((card) => (
                    <Card key={card.label} className="knowledge-quality-stat-card">
                        <ShieldCheck size={18} />
                        <strong>{card.value}</strong>
                        <h2>{card.label}</h2>
                        <p>{card.note}</p>
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
                        {trendCards.map((card) => (
                            <article key={card.label} className="knowledge-quality-trend-card">
                                <span>{card.label}</span>
                                <strong>{card.value}</strong>
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
                        {issueCards.map((issue) => (
                            <article key={issue} className="knowledge-quality-issue-card">
                                <p>{issue}</p>
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
                        {sourceRows.map((row) => (
                            <div key={row.source} className="knowledge-quality-source-row">
                                <div>
                                    <strong>{row.source}</strong>
                                    <span>{row.updatedAt}</span>
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
