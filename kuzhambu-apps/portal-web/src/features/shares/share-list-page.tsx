import { Link } from "react-router-dom";

export const ShareListPage = () => {
    return (
        <main className="portal-shell">
            <header className="portal-header">
                <div>
                    <p className="portal-kicker">Shares</p>
                    <h1>分享列表</h1>
                </div>
                <Link className="portal-action" to="/">
                    返回首页
                </Link>
            </header>
            <section className="portal-empty" aria-label="分享列表">
                分享列表查询将在下一步接入。
            </section>
        </main>
    );
};
