import { Bookmark, ChevronRight, Columns3, Database, GitFork } from "lucide-react";
import { Link } from "react-router-dom";
import entryFenghuangImage from "@/assets/home/portal-home-entry-fenghuang.png";
import entryHuntianyiImage from "@/assets/home/portal-home-entry-huntianyi.png";
import entryTaishanImage from "@/assets/home/portal-home-entry-taishan.png";
import entryXiandingImage from "@/assets/home/portal-home-entry-xianding.png";
import topicGuduImage from "@/assets/home/portal-home-topic-gudu.png";
import topicQiyongImage from "@/assets/home/portal-home-topic-qiyong.png";
import topicShanchuanImage from "@/assets/home/portal-home-topic-shanchuan.png";
import topicTianwenImage from "@/assets/home/portal-home-topic-tianwen.png";

const valueStatements = [
    {
        title: "开放浏览",
        description: "开放获取，自由阅读与检索。",
        icon: Columns3
    },
    {
        title: "精选条目",
        description: "权威整理，持续增订与校勘。",
        icon: Bookmark
    },
    {
        title: "来源可追溯",
        description: "标注原书与版本，可溯源可验证。",
        icon: Database
    }
];

const latestEntries = [
    {
        title: "凤凰",
        meta: "三才图会 · 鸟兽 · 百鸟部",
        description: "黄帝时，凤凰集于岐山之阳，其羽五色各异……",
        date: "05-18",
        href: "/classics/sancai",
        image: entryFenghuangImage
    },
    {
        title: "汁梁",
        meta: "三才图会 · 器用 · 高筵部",
        description: "汁梁，古汁州也。周显德初为东京……",
        date: "05-17",
        href: "/classics/sancai",
        image: entryXiandingImage
    },
    {
        title: "浑天仪",
        meta: "三才图会 · 星历 · 仪器部",
        description: "浑天之象，连轮无穷。以测天体，审度时刻……",
        date: "05-16",
        href: "/classics/sancai",
        image: entryHuntianyiImage
    },
    {
        title: "泰山",
        meta: "三才图会 · 山川 · 五岳部",
        description: "泰山，东岳也。高大雄尊，五岳之长……",
        date: "05-15",
        href: "/classics/sancai",
        image: entryTaishanImage
    }
];

const recommendedTopics = [
    {
        title: "山川总览",
        description: "汇集天下山川图记，地理形势与沿革考证。",
        href: "/discovery/search",
        image: topicShanchuanImage
    },
    {
        title: "古都图志",
        description: "历代都城图景与建置沿革，考镜古今。",
        href: "/knowledge/atlas",
        image: topicGuduImage
    },
    {
        title: "天文历法",
        description: "星象、历法与时令制度，观测与推演之学。",
        href: "/classics/sancai",
        image: topicTianwenImage
    },
    {
        title: "器用图考",
        description: "衣食住行与器物制度，图绘与名物考释。",
        href: "/discovery/search",
        image: topicQiyongImage
    }
];

const hotSignals = [
    { title: "长安与洛阳：两京建置与城市格局比较", count: "86 条相关" },
    { title: "大运河的开凿与漕运制度演变", count: "72 条相关" },
    { title: "古代兵器图谱与实战应用", count: "64 条相关" },
    { title: "二十八宿在历法与方位中的应用", count: "58 条相关" },
    { title: "古代名山考：五岳以外的名山体系", count: "47 条相关" },
    { title: "历代书院沿革与地域分布", count: "41 条相关" }
];

export const HomePage = () => {
    return (
        <>
            <section className="portal-effect-hero" aria-labelledby="portal-home-title">
                <div className="portal-effect-hero-copy">
                    <h1 id="portal-home-title">让古籍、图像与知识线索互相照见</h1>
                    <p>面向阅读、整理与研究的开放古籍知识门户。</p>
                    <div className="portal-effect-actions">
                        <Link className="portal-effect-primary" to="/classics/sancai">
                            浏览三才图会
                        </Link>
                        <Link className="portal-effect-secondary" to="/discovery/search">
                            进入知识检索
                        </Link>
                    </div>
                    <div className="portal-effect-values" aria-label="门户说明">
                        {valueStatements.map((item) => {
                            const Icon = item.icon;
                            return (
                                <article key={item.title}>
                                    <Icon aria-hidden="true" size={42} strokeWidth={1.25} />
                                    <div>
                                        <h2>{item.title}</h2>
                                        <p>{item.description}</p>
                                    </div>
                                </article>
                            );
                        })}
                    </div>
                </div>
            </section>

            <section className="portal-effect-content" aria-label="门户内容">
                <article className="portal-effect-panel portal-effect-latest">
                    <PanelHeading href="/classics/sancai" title="最新条目" />
                    <div className="portal-effect-entry-list">
                        {latestEntries.map((entry) => (
                            <Link key={entry.title} className="portal-effect-entry" to={entry.href}>
                                <img alt={`${entry.title}图像`} src={entry.image} />
                                <span>
                                    <strong>{entry.title}</strong>
                                    <small>{entry.meta}</small>
                                    <em>{entry.description}</em>
                                </span>
                                <time>{entry.date}</time>
                            </Link>
                        ))}
                    </div>
                </article>

                <article className="portal-effect-panel portal-effect-topics">
                    <PanelHeading href="/knowledge/atlas" title="推荐专题" />
                    <div className="portal-effect-topic-list">
                        {recommendedTopics.map((topic) => (
                            <Link key={topic.title} className="portal-effect-topic" to={topic.href}>
                                <img alt={`${topic.title}图像`} src={topic.image} />
                                <span>
                                    <strong>{topic.title}</strong>
                                    <small>{topic.description}</small>
                                </span>
                            </Link>
                        ))}
                    </div>
                </article>

                <article className="portal-effect-panel portal-effect-hot">
                    <PanelHeading href="/discovery/search" title="热门线索" />
                    <div className="portal-effect-hot-list">
                        {hotSignals.map((signal) => (
                            <Link key={signal.title} to="/discovery/search">
                                <GitFork aria-hidden="true" size={19} strokeWidth={1.25} />
                                <span>{signal.title}</span>
                                <small>{signal.count}</small>
                            </Link>
                        ))}
                    </div>
                </article>
            </section>
        </>
    );
};

const PanelHeading = ({ href, title }: { href: string; title: string }) => (
    <header className="portal-effect-panel-heading">
        <h2>{title}</h2>
        <Link to={href}>
            更多
            <ChevronRight aria-hidden="true" size={14} />
        </Link>
    </header>
);
