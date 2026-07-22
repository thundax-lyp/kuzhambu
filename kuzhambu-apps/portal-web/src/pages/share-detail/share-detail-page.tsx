import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Download, Eye } from "lucide-react";
import { Link, useParams } from "react-router-dom";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import * as shareDetailService from "./share-detail-service";
import type {
    ClassicsSharePortalImage,
    ClassicsSharePortalTarget,
    ClassicsShareResource,
    ClassicsShareTargetStatus
} from "./share-detail-types";

const CONTENT_TYPE_LABELS = new Map([
    ["SANCAI_ENTRY", "三才图会"],
    ["WANGQI_DOCUMENT", "王圻文档"],
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

const isDeletedTarget = (target: ClassicsSharePortalTarget) => {
    return target.targetStatus === "CONTENT_DELETED";
};

const readTargetStatusLabel = (status?: ClassicsShareTargetStatus | string | null) => {
    return (
        {
            ACTIVE: "可用",
            AVAILABLE: "可用",
            CONTENT_DELETED: "内容已删除"
        }[status || ""] || "未知"
    );
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
    mode: "download" | "preview",
    privateAccess: boolean
) => {
    if (!resource?.storageObjectId) {
        return undefined;
    }
    return shareDetailService.getShareResourceContentUrl({
        mode,
        privateAccess,
        shareToken: token,
        storageObjectId: resource.storageObjectId
    });
};

const resolveImageUrl = (
    token: string,
    image: ClassicsSharePortalImage,
    mode: "download" | "preview",
    privateAccess: boolean
) => {
    const directUrl = mode === "download" ? image.downloadUrl : image.previewUrl;
    if (directUrl && !privateAccess) {
        return directUrl;
    }
    const resourceUrl =
        mode === "download" ? image.storageObject?.downloadUrl : image.storageObject?.previewUrl;
    return !privateAccess && resourceUrl
        ? resourceUrl
        : resolveResourceUrl(token, image.storageObject, mode, privateAccess);
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

const renderWangqiResource = (
    token: string,
    resource: ClassicsShareResource | null | undefined,
    privateAccess: boolean
) => {
    if (!resource?.storageObjectId) {
        return null;
    }
    const previewUrl = resolveResourceUrl(token, resource, "preview", privateAccess);
    const downloadUrl = resolveResourceUrl(token, resource, "download", privateAccess);
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

const getSancaiImageKey = (image: ClassicsSharePortalImage, index: number) => {
    return `${image.imageId ?? "image"}-${image.storageObjectId ?? image.storageObject?.storageObjectId ?? index}`;
};

const getSancaiImageTitle = (image: ClassicsSharePortalImage) => {
    return image.title || image.originalFilename || `图片 ${image.imageId ?? "-"}`;
};

const sortSancaiImages = (images: ClassicsSharePortalImage[]) => {
    return [...images].sort((left, right) => {
        const leftPriority = left.priority ?? Number.MAX_SAFE_INTEGER;
        const rightPriority = right.priority ?? Number.MAX_SAFE_INTEGER;
        if (leftPriority !== rightPriority) {
            return leftPriority - rightPriority;
        }
        return (left.imageId ?? 0) - (right.imageId ?? 0);
    });
};

const SancaiImageGallery = ({
    images,
    privateAccess,
    token
}: {
    images?: ClassicsSharePortalImage[] | null;
    privateAccess: boolean;
    token: string;
}) => {
    const visibleImages = sortSancaiImages(
        (images || []).filter((image) => image.storageObject?.storageObjectId)
    );
    const defaultImage = visibleImages.find((image) => image.currentUsed) ?? visibleImages[0];
    const [selectedImageKey, setSelectedImageKey] = useState<string | null>(null);
    const selectedImage =
        visibleImages.find(
            (image, index) => getSancaiImageKey(image, index) === selectedImageKey
        ) ?? defaultImage;
    if (!visibleImages.length) {
        return null;
    }
    const selectedPreviewUrl = selectedImage
        ? resolveImageUrl(token, selectedImage, "preview", privateAccess)
        : undefined;
    const selectedDownloadUrl = selectedImage
        ? resolveImageUrl(token, selectedImage, "download", privateAccess)
        : undefined;
    const selectedTitle = selectedImage ? getSancaiImageTitle(selectedImage) : "三才图会图片";

    return (
        <section className="portal-share-images" aria-label="三才图会图片">
            <h3>三才图会图片</h3>
            <figure className="portal-share-image-featured">
                {selectedPreviewUrl ? (
                    <img src={selectedPreviewUrl} alt={selectedTitle} />
                ) : (
                    <div className="portal-share-image-placeholder">图片暂不可预览</div>
                )}
                <figcaption>
                    <div>
                        <strong>{selectedTitle}</strong>
                        <span>{formatFileSize(selectedImage?.size)}</span>
                    </div>
                    {selectedDownloadUrl ? (
                        <Button asChild size="sm" variant="outline">
                            <a href={selectedDownloadUrl} target="_blank" rel="noreferrer">
                                <Download aria-hidden="true" />
                                下载原图
                            </a>
                        </Button>
                    ) : null}
                </figcaption>
            </figure>
            <div className="portal-share-image-thumbnails" aria-label="三才图会图片缩略图">
                {visibleImages.map((image, index) => {
                    const imageKey = getSancaiImageKey(image, index);
                    const previewUrl = resolveImageUrl(token, image, "preview", privateAccess);
                    const title = getSancaiImageTitle(image);
                    const selected = selectedImage === image;
                    return (
                        <button
                            aria-current={selected ? "true" : undefined}
                            aria-label={`切换图片 ${title}`}
                            className="portal-share-image-thumb"
                            key={imageKey}
                            type="button"
                            onClick={() => setSelectedImageKey(imageKey)}
                        >
                            {previewUrl ? <img src={previewUrl} alt="" /> : <span>无预览</span>}
                            <strong>{title}</strong>
                            {image.currentUsed ? <span>当前使用</span> : null}
                        </button>
                    );
                })}
            </div>
        </section>
    );
};

const renderShareTargetCard = (
    target: ClassicsSharePortalTarget,
    index: number,
    token: string,
    privateAccess: boolean
) => {
    const deleted = isDeletedTarget(target);
    return (
        <Card
            aria-label={`${readTargetTitle(target, index)}内容卡片`}
            className={cn("portal-share-target", deleted && "portal-share-target-deleted")}
            key={`${target.contentType}-${target.contentId}-${target.priority}-${index}`}
        >
            <header>
                <div>
                    <p>{formatContentType(target.contentType)}</p>
                    <h2>{readTargetTitle(target, index)}</h2>
                </div>
                <Badge
                    className="portal-share-version"
                    variant={deleted ? "destructive" : "secondary"}
                >
                    {deleted
                        ? readTargetStatusLabel(target.targetStatus)
                        : `v${target.contentVersionNo ?? "-"}`}
                </Badge>
            </header>
            {deleted ? (
                <>
                    <dl>
                        <div>
                            <dt>内容 ID</dt>
                            <dd>{target.contentId ?? "-"}</dd>
                        </div>
                        <div>
                            <dt>目标状态</dt>
                            <dd>{readTargetStatusLabel(target.targetStatus)}</dd>
                        </div>
                    </dl>
                    <p className="portal-share-deleted-placeholder">
                        内容已删除，分享仅保留标题快照。
                    </p>
                </>
            ) : (
                <>
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
                            <dd>{readTargetStatusLabel(target.targetStatus)}</dd>
                        </div>
                    </dl>
                    {target.contentType === "WANGQI_DOCUMENT"
                        ? renderWangqiResource(token, target.storageObject, privateAccess)
                        : null}
                    {target.contentType === "SANCAI_ENTRY" ? (
                        <SancaiImageGallery
                            privateAccess={privateAccess}
                            token={token}
                            images={target.images}
                        />
                    ) : null}
                    {renderSnapshotSummary(target)}
                </>
            )}
        </Card>
    );
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
                            targets.map((target, index) =>
                                renderShareTargetCard(target, index, token, privateAccess)
                            )
                        ) : (
                            <Card className="portal-empty">暂无分享快照。</Card>
                        )}
                    </section>
                </>
            ) : null}
        </main>
    );
};
