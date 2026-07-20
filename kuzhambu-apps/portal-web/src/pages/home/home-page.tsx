import { BookOpen, LibraryBig, Network, Search, Share2 } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

const sections = [
    {
        title: "三才图会",
        description: "浏览已发布的三才图会条目、译文与配图",
        href: "/classics/sancai",
        icon: BookOpen
    },
    {
        title: "分享内容",
        description: "浏览公开分享的古籍版本快照",
        href: "/shares",
        icon: Share2
    },
    {
        title: "知识图谱馆",
        description: "进入知识首页，查看图谱、质量与来源导览",
        href: "/knowledge",
        icon: LibraryBig
    },
    {
        title: "知识检索",
        description: "围绕实体、标签和关系组织检索",
        href: "/discovery/search",
        icon: Search
    },
    {
        title: "问答工作台",
        description: "先建会话，再看来源、轨迹与回答",
        href: "/discovery/qa",
        icon: BookOpen
    },
    {
        title: "关系探索",
        description: "承接知识图谱与问答场景",
        href: "/discovery/search",
        icon: Network
    }
];

export const HomePage = () => {
    return (
        <main className="portal-shell">
            <header className="portal-header">
                <div>
                    <p className="portal-kicker">三才翰典 Portal</p>
                    <h1>古籍知识门户</h1>
                </div>
                <div className="portal-header-actions">
                    <Button asChild className="portal-action" size="lg" variant="outline">
                        <Link to="/classics/sancai">进入三才图会</Link>
                    </Button>
                    <Button asChild className="portal-action" size="lg" variant="outline">
                        <Link to="/knowledge">进入知识馆</Link>
                    </Button>
                    <Button asChild className="portal-action" size="lg" variant="outline">
                        <Link to="/discovery/search">进入检索</Link>
                    </Button>
                    <Button asChild className="portal-action" size="lg" variant="outline">
                        <Link to="/discovery/qa">进入问答</Link>
                    </Button>
                </div>
            </header>

            <section className="portal-home-search" aria-label="首页知识检索入口">
                <Link className="portal-home-search-link" to="/discovery/search">
                    <Search aria-hidden="true" size={22} />
                    <span>搜索古籍、人物、礼制、图谱线索</span>
                    <strong>搜索</strong>
                </Link>
            </section>

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
