import { useQuery } from "@tanstack/react-query";
import { ArrowRight, Binary, ChartSpline, GitBranch, ScrollText, Sparkles } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import * as KnowledgeHomeService from "./knowledge-home-service";

import "./knowledge-home-page.css";

export const KnowledgeHomePage = () => {
    const homeQuery = useQuery({
        queryFn: KnowledgeHomeService.getKnowledgeHome,
        queryKey: ["knowledge-home"]
    });
    const content = homeQuery.data ?? KnowledgeHomeService.KNOWLEDGE_HOME_FALLBACK;

    return (
        <main className="knowledge-shell">
            <section className="knowledge-hero">
                <div className="knowledge-hero-copy">
                    <p className="knowledge-kicker">Knowledge Atlas</p>
                    <h1>{content.heroTitle}</h1>
                    <p className="knowledge-lead">{content.heroSubtitle}</p>
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
                    <div className="knowledge-search-pill">{content.searchPlaceholder}</div>
                </Card>
            </section>

            <section className="knowledge-stat-grid" aria-label="知识概览">
                {content.stats.map((stat) => (
                    <Card key={stat.key} className="knowledge-stat-card">
                        <p className="knowledge-stat-value">{stat.value}</p>
                        <h2>{stat.label}</h2>
                        <p>{stat.deltaText}</p>
                    </Card>
                ))}
            </section>

            <section className="knowledge-guide-grid" aria-label="知识导览入口">
                {content.quickLinks.map((guide) => {
                    let Icon = Binary;
                    if (guide.type === "quality") {
                        Icon = ChartSpline;
                    }
                    if (guide.type === "lineage") {
                        Icon = GitBranch;
                    }

                    return (
                        <Link key={guide.key} className="knowledge-guide-link" to={guide.href}>
                            <Card className="knowledge-guide-card">
                                <div className="knowledge-guide-icon">
                                    <Icon size={20} />
                                </div>
                                <div>
                                    <h2>{guide.label}</h2>
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
                    {content.recentUpdates.map((item) => (
                        <Card key={item.title} className="knowledge-update-card">
                            <span className="knowledge-update-stamp">
                                {item.subtitle || "最近整理"}
                            </span>
                            <h3>{item.title}</h3>
                            <p>{item.summary}</p>
                        </Card>
                    ))}
                </div>
            </section>
        </main>
    );
};
