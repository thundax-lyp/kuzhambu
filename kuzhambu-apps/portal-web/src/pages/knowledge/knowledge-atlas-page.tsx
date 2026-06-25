import { useQuery } from "@tanstack/react-query";
import { ArrowRight, Binary, Filter, Orbit, ScrollText } from "lucide-react";
import { Link } from "react-router-dom";
import { Card } from "@/components/ui/card";
import { KNOWLEDGE_ATLAS_FALLBACK, getKnowledgeAtlas } from "./knowledge-atlas-service";

import "./knowledge-atlas-page.css";

export const KnowledgeAtlasPage = () => {
    const atlasQuery = useQuery({
        queryFn: getKnowledgeAtlas,
        queryKey: ["knowledge-atlas"]
    });
    const content = atlasQuery.data ?? KNOWLEDGE_ATLAS_FALLBACK;
    const filterGroups = [
        { label: "知识库", values: content.availableFilters.knowledgeBases },
        { label: "实体类型", values: content.availableFilters.entityTypes },
        { label: "时间范围", values: content.availableFilters.timeRanges }
    ];

    return (
        <main className="knowledge-atlas-shell">
            <header className="knowledge-atlas-header">
                <div>
                    <p className="knowledge-atlas-kicker">Knowledge Atlas</p>
                    <h1>图谱浏览台</h1>
                    <p>
                        先筛选知识范围，再沿实体关系、来源与时间线展开阅读。 这里承接真正的“筛选 -
                        浏览 - 详情”三层结构。
                    </p>
                </div>
                <Link className="knowledge-atlas-back" to="/knowledge">
                    返回知识首页
                    <ArrowRight size={16} />
                </Link>
            </header>

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
                            <h2>关系舞台</h2>
                        </div>
                    </div>

                    <div className="knowledge-atlas-focus">
                        <span>当前焦点</span>
                        <strong>{content.focusNode.title}</strong>
                        <small>
                            {content.focusNode.type} · {content.focusNode.summary} ·{" "}
                            {content.focusNode.status}
                        </small>
                    </div>

                    <div className="knowledge-atlas-bands">
                        {content.relationGroups.map((band) => (
                            <article key={band.groupKey} className="knowledge-atlas-band">
                                <Orbit size={18} />
                                <div>
                                    <h3>{band.groupLabel}</h3>
                                    <p>
                                        {band.relations[0]?.sourceLabel} →{" "}
                                        {band.relations[0]?.targetLabel} ·{" "}
                                        {band.relations[0]?.relationLabel}
                                    </p>
                                </div>
                            </article>
                        ))}
                    </div>
                </Card>

                <Card className="knowledge-atlas-detail-panel">
                    <div className="knowledge-atlas-panel-heading">
                        <ScrollText size={18} />
                        <div>
                            <p>详情</p>
                            <h2>焦点说明</h2>
                        </div>
                    </div>

                    <section className="knowledge-atlas-detail-block">
                        <h3>
                            {content.focusNode.title} · {content.focusNode.type}
                        </h3>
                        <p>
                            {content.focusNode.summary}
                            。详情栏会承接实体摘要、来源说明、时间线和相关标签，帮助用户不离开页面就理解当前焦点。
                        </p>
                    </section>

                    <section className="knowledge-atlas-detail-block">
                        <h3>当前线索</h3>
                        <ul>
                            {content.timelineItems.map((note) => (
                                <li key={note.timeLabel}>
                                    {note.timeLabel}：{note.title}
                                </li>
                            ))}
                        </ul>
                    </section>
                </Card>
            </section>
        </main>
    );
};
