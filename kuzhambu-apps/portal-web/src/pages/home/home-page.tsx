import { BookOpen, Network, Search, Share2 } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

const sections = [
    {
        title: "分享内容",
        description: "浏览公开分享的古籍版本快照",
        href: "/shares",
        icon: Share2
    },
    { title: "典籍浏览", description: "面向读者的古籍内容入口", href: "/shares", icon: BookOpen },
    {
        title: "知识检索",
        description: "围绕实体、标签和关系组织检索",
        href: "/shares",
        icon: Search
    },
    { title: "关系探索", description: "承接知识图谱与问答场景", href: "/shares", icon: Network }
];

export const HomePage = () => {
    return (
        <main className="portal-shell">
            <header className="portal-header">
                <div>
                    <p className="portal-kicker">三才翰典 Portal</p>
                    <h1>古籍知识门户</h1>
                </div>
                <Button asChild className="portal-action" size="lg" variant="outline">
                    <Link to="/shares">进入分享</Link>
                </Button>
            </header>

            <section className="portal-grid" aria-label="门户能力">
                {sections.map((section) => {
                    const Icon = section.icon;

                    return (
                        <Link key={section.title} to={section.href}>
                            <Card className="portal-section">
                                <Icon aria-hidden="true" size={24} strokeWidth={1.8} />
                                <div>
                                    <h2>{section.title}</h2>
                                    <p>{section.description}</p>
                                </div>
                            </Card>
                        </Link>
                    );
                })}
            </section>
        </main>
    );
};
