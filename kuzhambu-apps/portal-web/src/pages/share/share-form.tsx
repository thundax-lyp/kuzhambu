import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import * as shareService from "./share-service";
import type { ClassicsSharePortalTarget } from "./share-types";

const CONTENT_TYPE_LABELS = new Map([
    ["SANCAI_ENTRY", "三才图会"],
    ["WANGQI_DOCUMENT", "万启文档"],
    ["MING_CUSTOMS", "明代民俗"]
]);

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "未设置";
    }

    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
};

const formatSnapshot = (snapshot?: string | null) => {
    if (!snapshot) {
        return "暂无快照内容";
    }

    try {
        return JSON.stringify(JSON.parse(snapshot), null, 2);
    } catch {
        return snapshot;
    }
};

const readTargetTitle = (target: ClassicsSharePortalTarget, index: number) => {
    return target.titleSnapshot?.trim() || `分享内容 ${index + 1}`;
};

const formatContentType = (value?: string | null) => {
    return value ? (CONTENT_TYPE_LABELS.get(value) ?? value) : "未知分类";
};

export const ShareForm = () => {
    const { shareToken } = useParams();
    const token = shareToken ?? "";
    const shareQuery = useQuery({
        enabled: token.length > 0,
        queryFn: () => shareService.getShare(token),
        queryKey: ["classics", "shares", token],
        retry: false
    });
    const share = shareQuery.data;
    const targets = share?.targets ?? [];

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

            {share ? (
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
                                <Card
                                    className="portal-share-target"
                                    key={`${target.contentType}-${target.contentId}-${target.priority}`}
                                >
                                    <header>
                                        <div>
                                            <p>{formatContentType(target.contentType)}</p>
                                            <h2>{readTargetTitle(target, index)}</h2>
                                        </div>
                                        <Badge className="portal-share-version" variant="secondary">
                                            v{target.contentVersionNo ?? "-"}
                                        </Badge>
                                    </header>
                                    <dl>
                                        <div>
                                            <dt>内容 ID</dt>
                                            <dd>{target.contentId ?? "-"}</dd>
                                        </div>
                                        <div>
                                            <dt>版本 ID</dt>
                                            <dd>{target.contentVersionId ?? "-"}</dd>
                                        </div>
                                        <div>
                                            <dt>内容可见性</dt>
                                            <dd>{target.contentVisibilitySnapshot || "-"}</dd>
                                        </div>
                                        <div>
                                            <dt>目标状态</dt>
                                            <dd>{target.targetStatus || "-"}</dd>
                                        </div>
                                    </dl>
                                    <pre>{formatSnapshot(target.contentSnapshotJson)}</pre>
                                </Card>
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
