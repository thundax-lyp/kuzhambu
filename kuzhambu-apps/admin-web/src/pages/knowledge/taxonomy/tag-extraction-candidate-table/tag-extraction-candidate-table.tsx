import { Table } from "antd";
import type { TableProps } from "antd";
import type { Key } from "react";
import type { TagExtractionCandidateRecord } from "../taxonomy-types";

interface TagExtractionCandidateTableProps {
    candidates: TagExtractionCandidateRecord[];
    selectedRowKeys: Key[];
    onSelectionChange: (selectedRowKeys: Key[]) => void;
}

const readCandidateKey = (candidate: TagExtractionCandidateRecord, index: number) => {
    return `${candidate.name || "tag"}-${candidate.matchedExistingTagId || "new"}-${index}`;
};

const formatConfidence = (value?: number | null) => {
    if (value === undefined || value === null) {
        return "-";
    }
    return `${Math.round(value * 100)}%`;
};

export const TagExtractionCandidateTable = ({
    candidates,
    selectedRowKeys,
    onSelectionChange
}: TagExtractionCandidateTableProps) => {
    const dataSource = candidates.map((candidate, index) => ({
        ...candidate,
        key: readCandidateKey(candidate, index)
    }));

    const columns: TableProps<TagExtractionCandidateRecord & { key: string }>["columns"] = [
        {
            title: "标签名",
            dataIndex: "name",
            key: "name",
            width: 140
        },
        {
            title: "分类",
            key: "category",
            width: 140,
            render: (_, candidate) => candidate.categoryName || candidate.categoryId || "-"
        },
        {
            title: "置信度",
            dataIndex: "confidence",
            key: "confidence",
            width: 100,
            render: formatConfidence
        },
        {
            title: "匹配标签",
            dataIndex: "matchedExistingTagId",
            key: "matchedExistingTagId",
            width: 140,
            render: (matchedExistingTagId?: string | null) => matchedExistingTagId || "新标签"
        },
        {
            title: "理由",
            dataIndex: "reason",
            key: "reason",
            ellipsis: true,
            render: (reason?: string | null) => reason || "-"
        }
    ];

    return (
        <Table
            aria-label="AI 标签候选"
            rowKey="key"
            size="small"
            columns={columns}
            dataSource={dataSource}
            pagination={false}
            rowSelection={{
                selectedRowKeys,
                onChange: onSelectionChange
            }}
            locale={{ emptyText: "暂无 AI 标签候选" }}
        />
    );
};
