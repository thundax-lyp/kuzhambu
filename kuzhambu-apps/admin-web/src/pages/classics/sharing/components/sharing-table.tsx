import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import type {
    ClassicsShareLinkStatus,
    ClassicsShareRecord,
    ClassicsShareTargetRecord,
    ClassicsShareVisibility
} from "@/pages/classics/common/classics-share-types";

type ShareStatusTone = "danger" | "neutral" | "success" | "warning";

const shareStatusTone: Record<string, ShareStatusTone> = {
    ACTIVE: "success",
    EXPIRED: "warning",
    REVOKED: "danger"
};

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return value;
    }
    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(timestamp));
};

const shareTypeLabel = (contentType?: string | null) => {
    if (contentType === "WANGQI_DOCUMENT") {
        return "王圻文档";
    }
    if (contentType === "SANCAI_ENTRY") {
        return "三才条目";
    }
    if (contentType === "MING_CUSTOMS") {
        return "明人志异";
    }
    return "未知类型";
};

const shareRecordLabel = (share: ClassicsShareRecord) => {
    const contentType = share.targets?.[0]?.contentType;
    return `${share.title || `分享 ${share.id}`}-${shareTypeLabel(contentType)}`;
};

const readStatusTagType = (status?: ClassicsShareLinkStatus | string | null) => {
    return shareStatusTone[status || ""] || "neutral";
};

const isExpiredAt = (expiresAt?: string | null) => {
    if (!expiresAt) {
        return false;
    }
    const timestamp = Date.parse(expiresAt);
    return Number.isNaN(timestamp) ? false : timestamp <= Date.now();
};

const canRestoreShare = (share?: ClassicsShareRecord | null) => {
    return share?.status === "REVOKED" && !isExpiredAt(share.expiresAt);
};

const readVisibilityLabel = (visibility?: ClassicsShareVisibility | string | null) => {
    return (
        {
            PRIVATE: "私有",
            PUBLIC: "公开"
        }[visibility || ""] || "未知"
    );
};

const readShareTypeByTargets = (targets?: ClassicsShareTargetRecord[] | null) => {
    return shareTypeLabel(targets?.[0]?.contentType);
};

interface SharingTableOptions {
    onStatusChange: (share: ClassicsShareRecord, status: ClassicsShareLinkStatus | string) => void;
    onView: (share: ClassicsShareRecord) => void;
}

export const createSharingTableColumns = ({
    onStatusChange,
    onView
}: SharingTableOptions): KuzhambuTableProps<ClassicsShareRecord>["columns"] => [
    {
        title: "标题",
        dataIndex: "title",
        key: "title",
        width: 220,
        render: (title?: string | null) => title || "-"
    },
    {
        title: "分享类型",
        dataIndex: "targets",
        key: "contentType",
        width: 150,
        render: (_targets, share) => readShareTypeByTargets(share?.targets)
    },
    {
        title: "状态",
        dataIndex: "status",
        key: "status",
        width: 120,
        render: (status?: ClassicsShareLinkStatus | string | null) => (
            <KuzhambuTag type={readStatusTagType(status)}>{status || "UNKNOWN"}</KuzhambuTag>
        )
    },
    {
        title: "可见性",
        dataIndex: "visibility",
        key: "visibility",
        width: 120,
        render: (visibility?: ClassicsShareVisibility | string | null) =>
            readVisibilityLabel(visibility)
    },
    {
        title: "访问次数",
        dataIndex: "accessCount",
        key: "accessCount",
        width: 120,
        render: (value?: number | null) => (typeof value === "number" ? value : "-")
    },
    {
        title: "发布时间",
        dataIndex: "issuedAt",
        key: "issuedAt",
        width: 190,
        render: formatDateTime
    },
    {
        title: "过期时间",
        dataIndex: "expiresAt",
        key: "expiresAt",
        width: 190,
        render: formatDateTime
    },
    {
        key: "actions",
        options: (share) => {
            const statusActions = [];
            if (share.status === "ACTIVE") {
                statusActions.push({
                    key: "revoke",
                    text: "撤销",
                    ariaLabel: `撤销 ${shareRecordLabel(share)}`,
                    type: "danger" as const,
                    onClick: () => onStatusChange(share, "REVOKED")
                });
            } else if (canRestoreShare(share)) {
                statusActions.push({
                    key: "restore",
                    text: "恢复",
                    ariaLabel: `恢复 ${shareRecordLabel(share)}`,
                    onClick: () => onStatusChange(share, "ACTIVE")
                });
            }
            return [
                {
                    key: "detail",
                    text: "查看",
                    ariaLabel: `查看 ${shareRecordLabel(share)}`,
                    onClick: onView
                },
                ...(statusActions.length ? [{ type: "divider" as const }, ...statusActions] : [])
            ];
        }
    }
];
