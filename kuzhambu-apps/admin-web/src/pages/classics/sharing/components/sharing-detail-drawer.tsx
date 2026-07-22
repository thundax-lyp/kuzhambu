import { Descriptions, Tag, Typography } from "antd";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { DEFAULT_PAGE_SIZE } from "@/types/page";
import { SharingAccessActions } from "./sharing-access-actions";
import type {
    ClassicsShareAccessClientSnapshot,
    ClassicsShareAccessRecord,
    ClassicsShareLinkStatus,
    ClassicsShareRecord,
    ClassicsShareTargetRecord,
    ClassicsShareTargetStatus,
    ClassicsShareVisibility
} from "@/pages/classics/common/classics-share-types";

const { Text } = Typography;

type ShareStatusTone = "danger" | "neutral" | "success" | "warning";

const shareStatusTone: Record<string, ShareStatusTone> = {
    ACTIVE: "success",
    EXPIRED: "warning",
    REVOKED: "danger"
};

const shareTargetStatusTone: Record<string, ShareStatusTone> = {
    AVAILABLE: "success",
    CONTENT_DELETED: "danger"
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

const readStatusTagType = (status?: ClassicsShareLinkStatus | string | null) => {
    return shareStatusTone[status || ""] || "neutral";
};

const readTargetStatusLabel = (status?: ClassicsShareTargetStatus | string | null) => {
    return (
        {
            AVAILABLE: "可用",
            CONTENT_DELETED: "内容已删除"
        }[status || ""] || "未知"
    );
};

const readTargetStatusTagType = (status?: ClassicsShareTargetStatus | string | null) => {
    return shareTargetStatusTone[status || ""] || "neutral";
};

const isDeletedShareTarget = (target?: ClassicsShareTargetRecord | null) => {
    return target?.targetStatus === "CONTENT_DELETED";
};

const readVisibilityLabel = (visibility?: ClassicsShareVisibility | string | null) => {
    return (
        {
            PRIVATE: "私有",
            PUBLIC: "公开"
        }[visibility || ""] || "未知"
    );
};

const parseAccessClientSnapshot = (
    snapshot?: string | null
): ClassicsShareAccessClientSnapshot | null => {
    if (!snapshot) {
        return null;
    }
    try {
        return JSON.parse(snapshot) as ClassicsShareAccessClientSnapshot;
    } catch {
        return null;
    }
};

const readAccessTypeLabel = (snapshot?: string | null) => {
    const accessType = parseAccessClientSnapshot(snapshot)?.accessType;
    if (accessType === "DETAIL_VIEW") {
        return "详情浏览";
    }
    if (accessType === "RESOURCE_READ") {
        return "资源读取";
    }
    return "未知";
};

const targetColumns: KuzhambuTableProps<ClassicsShareTargetRecord>["columns"] = [
    {
        title: "内容类型",
        dataIndex: "contentType",
        key: "contentType",
        width: 150,
        render: (contentType?: string | null) => shareTypeLabel(contentType)
    },
    {
        title: "内容 ID",
        dataIndex: "contentId",
        key: "contentId",
        width: 120,
        render: (contentId?: number | null) => contentId ?? "-"
    },
    {
        title: "标题快照",
        dataIndex: "titleSnapshot",
        key: "titleSnapshot",
        render: (titleSnapshot?: string | null) => titleSnapshot || "-"
    },
    {
        title: "目标状态",
        dataIndex: "targetStatus",
        key: "targetStatus",
        width: 130,
        render: (status?: ClassicsShareTargetStatus | string | null) => (
            <KuzhambuTag type={readTargetStatusTagType(status)}>
                {readTargetStatusLabel(status)}
            </KuzhambuTag>
        )
    }
];

const accessRecordColumns: KuzhambuTableProps<ClassicsShareAccessRecord>["columns"] = [
    {
        title: "访问时间",
        dataIndex: "accessedAt",
        key: "accessedAt",
        width: 180,
        render: formatDateTime
    },
    {
        title: "访问类型",
        dataIndex: "clientSnapshot",
        key: "accessType",
        width: 120,
        render: readAccessTypeLabel
    },
    {
        title: "结果",
        dataIndex: "accessResult",
        key: "accessResult",
        width: 120,
        render: (result?: string | null) => <Tag>{result || "UNKNOWN"}</Tag>
    },
    {
        title: "目标 ID",
        dataIndex: "shareTargetId",
        key: "shareTargetId",
        width: 120,
        render: (shareTargetId?: number | null) => shareTargetId ?? "-"
    }
];

interface SharingDetailDrawerProps {
    accessPageNo: number;
    accessPageSize: number;
    accessRecords: ClassicsShareAccessRecord[];
    accessTotal: number;
    detailLoading: boolean;
    open: boolean;
    share: ClassicsShareRecord | undefined;
    targetRecords: ClassicsShareTargetRecord[];
    accessLoading: boolean;
    onAccessPageChange: (nextPageNo: number, nextPageSize?: number) => void;
    onClose: () => void;
    onRefreshAccessRecords: () => void;
    onStatusChange: (share: ClassicsShareRecord, status: ClassicsShareLinkStatus | string) => void;
}

export const SharingDetailDrawer = ({
    accessPageNo,
    accessPageSize,
    accessRecords,
    accessTotal,
    detailLoading,
    open,
    share,
    targetRecords,
    accessLoading,
    onAccessPageChange,
    onClose,
    onRefreshAccessRecords,
    onStatusChange
}: SharingDetailDrawerProps) => {
    return (
        <KuzhambuDrawer
            testId="classics-sharing-sharing-drawer"
            destroyOnClose
            open={open}
            title="分享详情"
            size="middle"
            onClose={onClose}
        >
            <KuzhambuSpace orientation="vertical" size="middle" style={{ width: "100%" }}>
                <Descriptions title="分享信息" bordered size="small" column={2}>
                    <Descriptions.Item label="标题">{share?.title || "-"}</Descriptions.Item>
                    <Descriptions.Item label="分享码">{share?.shareToken || "-"}</Descriptions.Item>
                    <Descriptions.Item label="状态">
                        <KuzhambuTag type={readStatusTagType(share?.status)}>
                            {share?.status || "UNKNOWN"}
                        </KuzhambuTag>
                    </Descriptions.Item>
                    <Descriptions.Item label="可见性">
                        {readVisibilityLabel(share?.visibility)}
                    </Descriptions.Item>
                    <Descriptions.Item label="可用访问次数">
                        {share?.accessCount ?? 0}
                    </Descriptions.Item>
                    <Descriptions.Item label="发布时间">
                        {formatDateTime(share?.issuedAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="过期时间">
                        {formatDateTime(share?.expiresAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="分享链接">{share?.shareUrl || "-"}</Descriptions.Item>
                </Descriptions>

                <SharingAccessActions
                    share={share}
                    onRefreshAccessRecords={onRefreshAccessRecords}
                    onStatusChange={onStatusChange}
                />

                <Text strong>关联内容</Text>
                <KuzhambuTable<ClassicsShareTargetRecord>
                    ariaLabel="分享目标列表"
                    rowKey="id"
                    rowSelection={undefined}
                    dataSource={targetRecords}
                    columns={targetColumns}
                    loading={detailLoading}
                    rowClassName={(target) =>
                        isDeletedShareTarget(target) ? "sharing-target-row-deleted" : ""
                    }
                    scroll={{ x: 760 }}
                />

                <Text strong>访问记录</Text>
                <KuzhambuTable<ClassicsShareAccessRecord>
                    ariaLabel="访问记录列表"
                    rowKey="id"
                    rowSelection={undefined}
                    dataSource={accessRecords}
                    loading={accessLoading}
                    columns={accessRecordColumns}
                    pagination={{
                        current: accessPageNo,
                        pageSize: accessPageSize || DEFAULT_PAGE_SIZE,
                        total: accessTotal,
                        pageSizeOptions: ["10", "20", "50", "100"],
                        onChange: onAccessPageChange,
                        showSizeChanger: true
                    }}
                />
            </KuzhambuSpace>
        </KuzhambuDrawer>
    );
};
