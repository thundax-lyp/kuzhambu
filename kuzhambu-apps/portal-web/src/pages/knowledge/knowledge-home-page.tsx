import { ArrowRight, Binary, ChartSpline, ScrollText, Sparkles } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

import "./knowledge-home-page.css";

const overviewStats = [
    { value: "12,480", label: "图谱实体", note: "人物、器物、礼制与典故共览" },
    { value: "3,126", label: "关系弧线", note: "从亲缘到制度脉络持续延展" },
    { value: "268", label: "已应用版本", note: "按版本追溯知识整理过程" }
];

const guides = [
    {
        title: "图谱浏览",
        description: "进入关系画布，沿实体、关系与时间线展开阅读。",
        href: "/knowledge/atlas",
        icon: Binary
    },
    {
        title: "质量总览",
        description: "查看确认率、来源构成与当前待处理事项。",
        href: "/knowledge/quality",
        icon: ChartSpline
    }
];

const recentUpdates = [
    {
        title: "三才图会 · 帝系知识快照",
        summary: "整理上古人物关系链，补齐黄帝相关亲缘与时代标签。",
        stamp: "今日更新"
    },
    {
        title: "明代习俗 · 岁时礼制增补",
        summary: "围绕节令和礼仪，新增一组制度关联与来源注记。",
        stamp: "两日前"
    }
];

export const KnowledgeHomePage = () => {
    return (
        <main className="knowledge-shell">
            <section className="knowledge-hero">
                <div className="knowledge-hero-copy">
                    <p className="knowledge-kicker">Knowledge Atlas</p>
                    <h1>古籍知识图谱馆</h1>
                    <p className="knowledge-lead">
                        把古籍中的人物、器物、礼制与来源脉络组织成可阅读的知识展陈。
                        这里不是治理后台，而是面向浏览与理解的知识入口。
                    </p>
                    <div className="knowledge-hero-actions">
                        <Button asChild className="knowledge-primary-action" size="lg">
                            <Link to="/knowledge/atlas">
                                进入图谱浏览
                                <ArrowRight size={16} />
                            </Link>
                        </Button>
                        <Button
                            asChild
                            className="knowledge-secondary-action"
                            size="lg"
                            variant="outline"
                        >
                            <Link to="/knowledge/quality">查看质量总览</Link>
                        </Button>
                    </div>
                </div>

                <Card className="knowledge-search-card">
                    <div className="knowledge-search-heading">
                        <Sparkles size={18} />
                        <span>馆藏导览</span>
                    </div>
                    <p>
                        从知识图谱浏览、质量总览和来源脉络三条路径进入，
                        先形成整体认知，再回到具体实体与版本。
                    </p>
                    <div className="knowledge-search-pill">人物 · 器物 · 礼制 · 典故 · 版本</div>
                </Card>
            </section>

            <section className="knowledge-stat-grid" aria-label="知识概览">
                {overviewStats.map((stat) => (
                    <Card key={stat.label} className="knowledge-stat-card">
                        <p className="knowledge-stat-value">{stat.value}</p>
                        <h2>{stat.label}</h2>
                        <p>{stat.note}</p>
                    </Card>
                ))}
            </section>

            <section className="knowledge-guide-grid" aria-label="知识导览入口">
                {guides.map((guide) => {
                    const Icon = guide.icon;

                    return (
                        <Link key={guide.title} className="knowledge-guide-link" to={guide.href}>
                            <Card className="knowledge-guide-card">
                                <div className="knowledge-guide-icon">
                                    <Icon size={20} />
                                </div>
                                <div>
                                    <h2>{guide.title}</h2>
                                    <p>{guide.description}</p>
                                </div>
                                <ArrowRight
                                    aria-hidden="true"
                                    className="knowledge-guide-arrow"
                                    size={18}
                                />
                            </Card>
                        </Link>
                    );
                })}
            </section>

            <section className="knowledge-updates" aria-label="最近更新">
                <div className="knowledge-section-heading">
                    <div>
                        <p>最近更新</p>
                        <h2>从最近一次知识整理切入</h2>
                    </div>
                    <ScrollText aria-hidden="true" size={22} />
                </div>
                <div className="knowledge-update-list">
                    {recentUpdates.map((item) => (
                        <Card key={item.title} className="knowledge-update-card">
                            <span className="knowledge-update-stamp">{item.stamp}</span>
                            <h3>{item.title}</h3>
                            <p>{item.summary}</p>
                        </Card>
                    ))}
                </div>
            </section>
        </main>
    );
};
