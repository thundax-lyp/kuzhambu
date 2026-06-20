import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import * as shareService from "@/api/share-service";

export const SharePage = () => {
    const { shareToken } = useParams();
    const token = shareToken ?? "";
    const shareQuery = useQuery({
        enabled: token.length > 0,
        queryFn: () => shareService.getShare(token),
        queryKey: ["classics", "shares", token],
        retry: false
    });

    return (
        <main className="portal-shell">
            <header className="portal-header">
                <div>
                    <p className="portal-kicker">Share</p>
                    <h1>{shareQuery.data?.title || "分享详情"}</h1>
                </div>
                <Link className="portal-action" to="/shares">
                    返回分享列表
                </Link>
            </header>
            <section className="portal-empty" aria-label="分享详情">
                {shareQuery.isLoading ? "正在加载分享内容" : null}
                {shareQuery.isError ? "分享内容不存在或已过期" : null}
                {shareQuery.data ? "分享快照展示将在下一步接入。" : null}
            </section>
        </main>
    );
};
