import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import * as shareService from "@/api/share-service";
import type { ClassicsSharePortalTarget } from "@/api/share-types";

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

export const SharePage = () => {
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
                    <p className="portal-kicker">Share</p>
                    <h1>{share?.title || "分享详情"}</h1>
                </div>
                <Link className="portal-action" to="/shares">
                    返回分享列表
                </Link>
            </header>

            {shareQuery.isLoading ? (
                <section className="portal-empty" aria-label="分享加载状态">
                    正在加载分享内容
                </section>
            ) : null}

            {shareQuery.isError ? (
                <section className="portal-empty" aria-label="分享错误状态">
                    分享内容不存在或已过期
                </section>
            ) : null}

            {share ? (
                <>
                    <section className="portal-share-meta" aria-label="分享信息">
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
                    </section>

                    <section className="portal-share-targets" aria-label="分享快照">
                        {targets.length ? (
                            targets.map((target, index) => (
                                <article
                                    className="portal-share-target"
                                    key={`${target.contentType}-${target.contentId}-${target.priority}`}
                                >
                                    <header>
                                        <div>
                                            <p>{target.contentType || "UNKNOWN"}</p>
                                            <h2>{readTargetTitle(target, index)}</h2>
                                        </div>
                                        <span>v{target.contentVersionNo ?? "-"}</span>
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
                                </article>
                            ))
                        ) : (
                            <div className="portal-empty">暂无分享快照。</div>
                        )}
                    </section>
                </>
            ) : null}
        </main>
    );
};
