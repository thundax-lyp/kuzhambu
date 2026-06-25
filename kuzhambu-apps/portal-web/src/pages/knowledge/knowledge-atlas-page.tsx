import { ArrowRight, Binary, Filter, Orbit, ScrollText } from "lucide-react";
import { Link } from "react-router-dom";
import { Card } from "@/components/ui/card";

import "./knowledge-atlas-page.css";

const filterGroups = [
    { label: "知识库", values: ["三才图会", "明代习俗", "王圻文档"] },
    { label: "实体类型", values: ["人物", "器物", "礼制", "典故"] },
    { label: "时间范围", values: ["最近 30 天", "最近 90 天", "全部版本"] }
];

const relationBands = [
    { title: "帝系关系", note: "从亲缘关系切入，查看上下代和配偶脉络。" },
    { title: "礼制关联", note: "围绕制度、器物和场景形成关联阅读路径。" },
    { title: "来源脉络", note: "回到版本与来源，理解知识如何被整理出来。" }
];

const sideNotes = ["确认状态：已人工确认", "来源快照：三才图会第 2 版", "最近整理：今日更新"];

export const KnowledgeAtlasPage = () => {
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
                        <strong>黄帝</strong>
                        <small>人物 · 上古始祖 · 已确认</small>
                    </div>

                    <div className="knowledge-atlas-bands">
                        {relationBands.map((band) => (
                            <article key={band.title} className="knowledge-atlas-band">
                                <Orbit size={18} />
                                <div>
                                    <h3>{band.title}</h3>
                                    <p>{band.note}</p>
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
                        <h3>黄帝 · 人物卡</h3>
                        <p>
                            详情栏会承接实体摘要、来源说明、时间线和相关标签，
                            帮助用户不离开页面就理解当前焦点。
                        </p>
                    </section>

                    <section className="knowledge-atlas-detail-block">
                        <h3>当前线索</h3>
                        <ul>
                            {sideNotes.map((note) => (
                                <li key={note}>{note}</li>
                            ))}
                        </ul>
                    </section>
                </Card>
            </section>
        </main>
    );
};
