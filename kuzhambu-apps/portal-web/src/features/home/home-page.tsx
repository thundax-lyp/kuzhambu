import { BookOpen, Network, Search, Share2 } from "lucide-react";
import { Link } from "react-router-dom";

const sections = [
    {
        title: "分享内容",
        description: "浏览公开分享的古籍快照",
        href: "/shares",
        icon: Share2
    },
    { title: "典籍浏览", description: "面向读者的古籍内容入口", href: "/shares", icon: BookOpen },
    { title: "知识检索", description: "围绕实体、标签和关系组织检索", href: "/shares", icon: Search },
    { title: "关系探索", description: "承接知识图谱与问答场景", href: "/shares", icon: Network }
];

export const HomePage = () => {
    return (
        <main className="portal-shell">
            <header className="portal-header">
                <div>
                    <p className="portal-kicker">Kuzhambu Portal</p>
                    <h1>古籍知识门户</h1>
                </div>
                <Link className="portal-action" to="/shares">
                    进入分享
                </Link>
            </header>

            <section className="portal-grid" aria-label="门户能力">
                {sections.map((section) => {
                    const Icon = section.icon;

                    return (
                        <Link className="portal-section" key={section.title} to={section.href}>
                            <Icon aria-hidden="true" size={24} strokeWidth={1.8} />
                            <div>
                                <h2>{section.title}</h2>
                                <p>{section.description}</p>
                            </div>
                        </Link>
                    );
                })}
            </section>
        </main>
    );
};
