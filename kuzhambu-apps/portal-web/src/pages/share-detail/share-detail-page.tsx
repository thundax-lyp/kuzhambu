import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ShareTargetCard } from "./components/share-target-card";
import * as shareDetailService from "./share-detail-service";

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "未设置";
    }

    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
};

export const ShareDetailPage = () => {
    const { shareToken } = useParams();
    const token = shareToken ?? "";
    const shareQuery = useQuery({
        enabled: token.length > 0,
        queryFn: () => shareDetailService.getAccessibleShare(token),
        queryKey: ["classics", "shares", token],
        retry: false
    });
    const share = shareQuery.data;
    const targets = share?.targets ?? [];
    const loginRequired = Boolean(share?.loginRequired);
    const privateAccess = share?.visibility === "PRIVATE" && !loginRequired;

    return (
        <main className="portal-shell">
            <header className="portal-header">
                <div>
                    <p className="portal-kicker">分享快照</p>
                    <h1>{share?.title || "分享详情"}</h1>
                </div>
                <Button asChild className="portal-action" size="lg" variant="outline">
                    <Link to="/shares">返回分享列表</Link>
                </Button>
            </header>

            {shareQuery.isLoading ? (
                <Card className="portal-empty" aria-label="分享加载状态">
                    正在加载分享内容
                </Card>
            ) : null}

            {shareQuery.isError ? (
                <Card className="portal-empty" aria-label="分享错误状态">
                    分享内容不存在或已过期
                </Card>
            ) : null}

            {loginRequired ? (
                <Card className="portal-empty" aria-label="私有分享登录引导">
                    私有分享需要登录后访问。请先登录后台账号，再重新打开此分享链接。
                </Card>
            ) : null}

            {share && !loginRequired ? (
                <>
                    <Card className="portal-share-meta" aria-label="分享信息">
                        <dl>
                            <div>
                                <dt>状态</dt>
                                <dd>{share.status || "-"}</dd>
                            </div>
                            <div>
                                <dt>可见性</dt>
                                <dd>{share.visibility || "-"}</dd>
                            </div>
                            <div>
                                <dt>分享时间</dt>
                                <dd>{formatDateTime(share.issuedAt)}</dd>
                            </div>
                            <div>
                                <dt>过期时间</dt>
                                <dd>{formatDateTime(share.expiresAt)}</dd>
                            </div>
                        </dl>
                    </Card>

                    <section className="portal-share-targets" aria-label="分享快照">
                        {targets.length ? (
                            targets.map((target, index) => (
                                <ShareTargetCard
                                    index={index}
                                    key={`${target.contentType}-${target.contentId}-${target.priority}-${index}`}
                                    onResolveResourceUrl={
                                        shareDetailService.getShareResourceContentUrl
                                    }
                                    privateAccess={privateAccess}
                                    target={target}
                                    token={token}
                                />
                            ))
                        ) : (
                            <Card className="portal-empty">暂无分享快照。</Card>
                        )}
                    </section>
                </>
            ) : null}
        </main>
    );
};
