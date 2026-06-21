import { useQuery } from "@tanstack/react-query";
import { Download, Eye } from "lucide-react";
import { Link, useParams } from "react-router-dom";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import * as shareService from "./share-service";
import type {
    ClassicsSharePortalImage,
    ClassicsSharePortalTarget,
    ClassicsShareResource
} from "./share-types";

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

const parseSnapshot = (snapshot?: string | null) => {
    if (!snapshot) {
        return null;
    }
    try {
        const parsed = JSON.parse(snapshot) as unknown;
        return parsed && typeof parsed === "object" && !Array.isArray(parsed)
            ? (parsed as Record<string, unknown>)
            : null;
    } catch {
        return null;
    }
};

const readTargetTitle = (target: ClassicsSharePortalTarget, index: number) => {
    return target.titleSnapshot?.trim() || `分享内容 ${index + 1}`;
};

const formatContentType = (value?: string | null) => {
    return value ? (CONTENT_TYPE_LABELS.get(value) ?? value) : "未知分类";
};

const readText = (snapshot: Record<string, unknown> | null, key: string) => {
    const value = snapshot?.[key];
    return typeof value === "string" && value.trim() ? value : null;
};

const formatFileSize = (size?: number | null) => {
    if (!size) {
        return "-";
    }
    if (size < 1024) {
        return `${size} B`;
    }
    if (size < 1024 * 1024) {
        return `${(size / 1024).toFixed(1)} KB`;
    }
    return `${(size / 1024 / 1024).toFixed(1)} MB`;
};

const resolveResourceUrl = (
    token: string,
    resource: ClassicsShareResource | null | undefined,
    mode: "download" | "preview"
) => {
    if (!resource?.storageObjectId) {
        return undefined;
    }
    return shareService.getShareResourceContentUrl({
        mode,
        shareToken: token,
        storageObjectId: resource.storageObjectId
    });
};

const renderSnapshotSummary = (target: ClassicsSharePortalTarget) => {
    const snapshot = parseSnapshot(target.contentSnapshotJson);
    if (!snapshot) {
        return <p className="portal-share-copy">暂无可展示的快照正文。</p>;
    }

    const fields =
        target.contentType === "SANCAI_ENTRY"
            ? [
                  ["摘要", readText(snapshot, "summary")],
                  ["原文", readText(snapshot, "originalText")],
                  ["译文", readText(snapshot, "translationText")]
              ]
            : [
                  ["摘要", readText(snapshot, "summary")],
                  ["正文", readText(snapshot, "content")],
                  ["文档时间", readText(snapshot, "documentTime")]
              ];
    const visibleFields = fields.filter(([, value]) => value);

    return visibleFields.length ? (
        <div className="portal-share-copy-list">
            {visibleFields.map(([label, value]) => (
                <section key={label}>
                    <h3>{label}</h3>
                    <p>{value}</p>
                </section>
            ))}
        </div>
    ) : (
        <p className="portal-share-copy">该快照暂无正文摘要。</p>
    );
};

const renderResourceMeta = (resource: ClassicsShareResource) => {
    return (
        <dl className="portal-share-resource-meta">
            <div>
                <dt>文件名</dt>
                <dd>{resource.originalFilename || `资源 ${resource.storageObjectId ?? "-"}`}</dd>
            </div>
            <div>
                <dt>类型</dt>
                <dd>{resource.contentType || "-"}</dd>
            </div>
            <div>
                <dt>大小</dt>
                <dd>{formatFileSize(resource.size)}</dd>
            </div>
        </dl>
    );
};

const renderWangqiResource = (token: string, resource?: ClassicsShareResource | null) => {
    if (!resource?.storageObjectId) {
        return null;
    }
    const previewUrl = resolveResourceUrl(token, resource, "preview");
    const downloadUrl = resolveResourceUrl(token, resource, "download");
    return (
        <section className="portal-share-resource" aria-label="王圻原始文件">
            <div>
                <h3>原始文件</h3>
                {renderResourceMeta(resource)}
            </div>
            <div className="portal-share-resource-actions">
                {previewUrl ? (
                    <Button asChild size="sm" variant="outline">
                        <a href={previewUrl} target="_blank" rel="noreferrer">
                            <Eye aria-hidden="true" />
                            预览
                        </a>
                    </Button>
                ) : null}
                {downloadUrl ? (
                    <Button asChild size="sm">
                        <a href={downloadUrl} target="_blank" rel="noreferrer">
                            <Download aria-hidden="true" />
                            下载
                        </a>
                    </Button>
                ) : null}
            </div>
        </section>
    );
};

const renderSancaiImages = (token: string, images?: ClassicsSharePortalImage[] | null) => {
    const visibleImages = (images || []).filter((image) => image.storageObject?.storageObjectId);
    if (!visibleImages.length) {
        return null;
    }
    return (
        <section className="portal-share-images" aria-label="三才图会图片">
            <h3>当前图片</h3>
            <div className="portal-share-image-grid">
                {visibleImages.map((image) => {
                    const previewUrl = resolveResourceUrl(token, image.storageObject, "preview");
                    return (
                        <figure key={`${image.imageId}-${image.storageObjectId}`}>
                            {previewUrl ? (
                                <a href={previewUrl} target="_blank" rel="noreferrer">
                                    <img
                                        src={previewUrl}
                                        alt={
                                            image.title || image.originalFilename || "三才图会图片"
                                        }
                                    />
                                </a>
                            ) : null}
                            <figcaption>
                                <strong>
                                    {image.title ||
                                        image.originalFilename ||
                                        `图片 ${image.imageId ?? "-"}`}
                                </strong>
                                <span>{formatFileSize(image.size)}</span>
                            </figcaption>
                        </figure>
                    );
                })}
            </div>
        </section>
    );
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
                                    {target.contentType === "WANGQI_DOCUMENT"
                                        ? renderWangqiResource(token, target.storageObject)
                                        : null}
                                    {target.contentType === "SANCAI_ENTRY"
                                        ? renderSancaiImages(token, target.images)
                                        : null}
                                    {renderSnapshotSummary(target)}
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
