import { BookOpen, Network, Search } from "lucide-react";

import "./styles.css";

const sections = [
    { title: "典籍浏览", description: "面向读者的古籍内容入口", icon: BookOpen },
    { title: "知识检索", description: "围绕实体、标签和关系组织检索", icon: Search },
    { title: "关系探索", description: "承接知识图谱与问答场景", icon: Network }
];

const portalApiBaseUrl = import.meta.env.VITE_PORTAL_API_BASE_URL || "/portal-api";

export function App() {
    return (
        <main className="portal-shell">
            <header className="portal-header">
                <div>
                    <p className="portal-kicker">Kuzhambu Portal</p>
                    <h1>古籍知识门户</h1>
                </div>
                <a className="portal-action" href={`${portalApiBaseUrl}/health`}>
                    接口探测
                </a>
            </header>

            <section className="portal-grid" aria-label="门户能力">
                {sections.map((section) => {
                    const Icon = section.icon;

                    return (
                        <article className="portal-section" key={section.title}>
                            <Icon aria-hidden="true" size={24} strokeWidth={1.8} />
                            <div>
                                <h2>{section.title}</h2>
                                <p>{section.description}</p>
                            </div>
                        </article>
                    );
                })}
            </section>
        </main>
    );
}
