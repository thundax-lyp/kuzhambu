import { Button, Empty, Input, Segmented, Select, Space, Typography } from "antd";
import type { WangqiDocumentQuery } from "../wangqi-service";
import type { WangqiDocumentRecord } from "../wangqi-types";

const { Text } = Typography;

const visibilityOptions = [
    { label: "全部", value: "" },
    { label: "公开", value: "PUBLIC" },
    { label: "私有", value: "PRIVATE" }
];

const sortOptions = [
    { label: "新到旧", value: "DESC" },
    { label: "旧到新", value: "ASC" }
];

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "未填写时间";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
};

export interface WangqiTimelinePanelProps {
    dataSource: WangqiDocumentRecord[];
    loading?: boolean;
    query: WangqiDocumentQuery;
    onOpenDocument: (record: WangqiDocumentRecord) => void;
    onQueryChange: (query: WangqiDocumentQuery) => void;
}

export const WangqiTimelinePanel = ({
    dataSource,
    loading = false,
    query,
    onOpenDocument,
    onQueryChange
}: WangqiTimelinePanelProps) => {
    const changeQuery = (patch: WangqiDocumentQuery) => {
        onQueryChange({
            ...query,
            ...patch
        });
    };

    return (
        <aside className="wangqi-timeline-panel" aria-label="王圻文档时间线">
            <Space className="wangqi-timeline-panel-controls" direction="vertical" size="small">
                <Input.Search
                    aria-label="搜索王圻时间线"
                    placeholder="搜索王圻文档标题、摘要或正文"
                    allowClear
                    value={query.keyword || ""}
                    onChange={(event) => changeQuery({ keyword: event.target.value || undefined })}
                    onSearch={(keyword) => changeQuery({ keyword: keyword || undefined })}
                />
                <div className="wangqi-timeline-panel-filter-row">
                    <Select
                        aria-label="王圻时间线可见性"
                        value={query.visibility || ""}
                        options={visibilityOptions}
                        onChange={(visibility) =>
                            changeQuery({ visibility: visibility || undefined })
                        }
                    />
                    <Segmented
                        aria-label="王圻时间线排序"
                        value={query.sortDirection || "DESC"}
                        options={sortOptions}
                        onChange={(sortDirection) =>
                            changeQuery({ sortDirection: sortDirection as "ASC" | "DESC" })
                        }
                    />
                </div>
            </Space>
            <div className="wangqi-timeline-panel-list" aria-busy={loading}>
                {dataSource.length === 0 ? (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无王圻时间线" />
                ) : (
                    dataSource.map((record) => (
                        <Button
                            key={record.id}
                            type="text"
                            className="wangqi-timeline-panel-item"
                            aria-label={`打开王圻文档 ${record.title || "未命名文档"}`}
                            onClick={() => onOpenDocument(record)}
                        >
                            <span className="wangqi-timeline-panel-item-time">
                                {formatDateTime(record.documentTime)}
                            </span>
                            <span className="wangqi-timeline-panel-item-title">
                                {record.title || "未命名文档"}
                            </span>
                            <Text
                                type="secondary"
                                ellipsis
                                className="wangqi-timeline-panel-item-summary"
                            >
                                {record.summary || "暂无摘要"}
                            </Text>
                        </Button>
                    ))
                )}
            </div>
        </aside>
    );
};
