import { Search } from "lucide-react";
import { Link, Outlet } from "react-router-dom";
import kuzhambuLogoImage from "@/assets/home/kuzhambu-logo.svg";
import footerMountainImage from "@/assets/home/portal-home-effect-footer-mountain.png";

const navigationItems = [
    { label: "三才图会", href: "/classics/sancai" },
    { label: "知识图谱", href: "/knowledge" },
    { label: "古籍检索", href: "/discovery/search" },
    { label: "公开分享", href: "/shares" },
    { label: "问答", href: "/discovery/qa" }
];

const footerGroups = [
    {
        title: "资源导航",
        links: ["三才图会", "山书与美书", "地方志", "金石碑刻"]
    },
    {
        title: "服务",
        links: ["古籍检索", "知识图谱", "图像浏览", "数据开放"]
    },
    {
        title: "研究支持",
        links: ["研究指南", "引用规范", "版本说明", "常见问题"]
    },
    {
        title: "社区与协作",
        links: ["公开分享", "问答", "整理与校勘协作", "意见反馈"]
    },
    {
        title: "关于我们",
        links: ["项目介绍", "团队", "版权与声明", "联系方式"]
    }
];

export const PortalLayout = () => {
    return (
        <div className="portal-shell portal-effect-layout">
            <header className="portal-effect-header">
                <Link className="portal-effect-brand" to="/" aria-label="Kuzhambu 首页">
                    <span>Kuzhambu</span>
                    <img className="portal-effect-brand-logo" src={kuzhambuLogoImage} alt="" />
                </Link>
                <nav className="portal-effect-nav" aria-label="门户导航">
                    {navigationItems.map((item) => (
                        <Link key={item.href} to={item.href}>
                            {item.label}
                        </Link>
                    ))}
                </nav>
                <Link
                    className="portal-effect-search"
                    to="/discovery/search"
                    aria-label="进入古籍检索"
                >
                    <Search aria-hidden="true" size={17} />
                    <span>搜索条目、图像、人物、地名、典籍...</span>
                    <strong>搜索</strong>
                </Link>
            </header>

            <Outlet />

            <footer className="portal-effect-footer">
                <img
                    className="portal-effect-footer-mountain"
                    alt=""
                    src={footerMountainImage}
                    aria-hidden="true"
                />
                <div className="portal-effect-footer-intro">
                    <span className="portal-effect-footer-seal" aria-hidden="true">
                        三才
                    </span>
                    <p>汇聚古籍与图像资源，连接知识与研究线索。</p>
                    <Link to="/knowledge">关于三才翰典</Link>
                </div>
                <nav className="portal-effect-footer-nav" aria-label="底部资源导航">
                    {footerGroups.map((group) => (
                        <section key={group.title}>
                            <h2>{group.title}</h2>
                            {group.links.map((link) => (
                                <Link key={link} to="/discovery/search">
                                    {link}
                                </Link>
                            ))}
                        </section>
                    ))}
                </nav>
            </footer>
        </div>
    );
};
