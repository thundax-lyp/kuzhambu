import { Select } from "antd";
import type { ShareFilters, ShareStatusFilter } from "./sharing-filter-config";

const SHARE_CONTENT_TYPE_OPTIONS = [
    { label: "全部", value: "ALL" },
    { label: "王圻文档", value: "WANGQI_DOCUMENT" },
    { label: "三才条目", value: "SANCAI_ENTRY" },
    { label: "明人志异", value: "MING_CUSTOMS" }
];

const SHARE_STATUS_OPTIONS: Array<{ label: string; value: ShareStatusFilter }> = [
    { label: "全部", value: "ALL" },
    { label: "生效", value: "ACTIVE" },
    { label: "已过期", value: "EXPIRED" },
    { label: "已撤销", value: "REVOKED" }
];

const SHARE_VISIBILITY_OPTIONS = [
    { label: "全部", value: "ALL" },
    { label: "公开", value: "PUBLIC" },
    { label: "私有", value: "PRIVATE" }
];

interface SharingFilterPanelProps {
    filters: ShareFilters;
    onChange: (filters: ShareFilters) => void;
}

export const SharingFilterPanel = ({ filters, onChange }: SharingFilterPanelProps) => {
    return [
        {
            name: "contentType",
            label: "内容类型",
            render: () => (
                <Select
                    aria-label="分享内容类型"
                    value={filters.contentType}
                    options={SHARE_CONTENT_TYPE_OPTIONS}
                    onChange={(contentType) =>
                        onChange({
                            ...filters,
                            contentType
                        })
                    }
                />
            )
        },
        {
            name: "status",
            label: "分享状态",
            render: () => (
                <Select
                    aria-label="分享状态"
                    value={filters.status}
                    options={SHARE_STATUS_OPTIONS}
                    onChange={(status) =>
                        onChange({
                            ...filters,
                            status: status || "ALL"
                        })
                    }
                />
            )
        },
        {
            name: "visibility",
            label: "可见性",
            render: () => (
                <Select
                    aria-label="分享可见性"
                    value={filters.visibility}
                    options={SHARE_VISIBILITY_OPTIONS}
                    onChange={(visibility) =>
                        onChange({
                            ...filters,
                            visibility: visibility || "ALL"
                        })
                    }
                />
            )
        }
    ];
};
