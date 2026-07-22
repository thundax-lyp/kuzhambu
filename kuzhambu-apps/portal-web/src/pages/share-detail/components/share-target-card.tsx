import { Download, Eye } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import { SancaiImageGallery } from "./sancai-image-gallery";
import type {
    ClassicsSharePortalTarget,
    ClassicsShareResource,
    ClassicsShareResourceContentUrlCommand,
    ClassicsShareTargetStatus
} from "@/pages/share-detail/share-detail-types";

const CONTENT_TYPE_LABELS = new Map([
    ["SANCAI_ENTRY", "三才图会"],
    ["WANGQI_DOCUMENT", "王圻文档"],
    ["MING_CUSTOMS", "明代民俗"]
]);

interface ShareTargetCardProps {
    index: number;
    onResolveResourceUrl: (command: ClassicsShareResourceContentUrlCommand) => string;
    privateAccess: boolean;
    target: ClassicsSharePortalTarget;
    token: string;
}

export const ShareTargetCard = ({
    index,
    onResolveResourceUrl,
    privateAccess,
    target,
    token
}: ShareTargetCardProps) => {
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
                    {target.contentType === "WANGQI_DOCUMENT" ? (
                        <WangqiResource
                            onResolveResourceUrl={onResolveResourceUrl}
                            privateAccess={privateAccess}
                            resource={target.storageObject}
                            token={token}
                        />
                    ) : null}
                    {target.contentType === "SANCAI_ENTRY" ? (
                        <SancaiImageGallery
                            onResolveResourceUrl={onResolveResourceUrl}
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

const readText = (snapshot: Record<string, unknown> | null, key: string) => {
    const value = snapshot?.[key];
    return typeof value === "string" && value.trim() ? value : null;
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
    privateAccess: boolean,
    onResolveResourceUrl: (command: ClassicsShareResourceContentUrlCommand) => string
) => {
    if (!resource?.storageObjectId) {
        return undefined;
    }
    return onResolveResourceUrl({
        mode,
        privateAccess,
        shareToken: token,
        storageObjectId: resource.storageObjectId
    });
};

const WangqiResource = ({
    onResolveResourceUrl,
    privateAccess,
    resource,
    token
}: {
    onResolveResourceUrl: (command: ClassicsShareResourceContentUrlCommand) => string;
    privateAccess: boolean;
    resource: ClassicsShareResource | null | undefined;
    token: string;
}) => {
    if (!resource?.storageObjectId) {
        return null;
    }
    const previewUrl = resolveResourceUrl(
        token,
        resource,
        "preview",
        privateAccess,
        onResolveResourceUrl
    );
    const downloadUrl = resolveResourceUrl(
        token,
        resource,
        "download",
        privateAccess,
        onResolveResourceUrl
    );
    return (
        <section className="portal-share-resource" aria-label="王圻原始文件">
            <div>
                <h3>原始文件</h3>
                <ResourceMeta resource={resource} />
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

const ResourceMeta = ({ resource }: { resource: ClassicsShareResource }) => {
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
