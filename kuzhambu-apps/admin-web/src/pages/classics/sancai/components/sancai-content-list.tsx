import { Empty, Skeleton, Typography } from "antd";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps, KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import type { SancaiContentRecord } from "../sancai-types";

const { Text } = Typography;

interface SancaiContentListProps {
    contents: SancaiContentRecord[];
    isLoading: boolean;
    onDelete: (content: SancaiContentRecord) => void;
    onEdit: (content: SancaiContentRecord) => void;
    onSort: (
        sourceContent: SancaiContentRecord,
        targetContent: SancaiContentRecord,
        position: KuzhambuTableSortPosition
    ) => void;
}

const readQuestion = (content: SancaiContentRecord) => {
    return content.question?.trim() || `内容 ${content.id}`;
};

export const SancaiContentList = ({
    contents,
    isLoading,
    onDelete,
    onEdit,
    onSort
}: SancaiContentListProps) => {
    if (isLoading) {
        return <Skeleton active paragraph={{ rows: 7 }} />;
    }

    if (!contents.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无条目内容" />;
    }

    const columns: KuzhambuTableProps<SancaiContentRecord>["columns"] = [
        {
            title: "问题",
            key: "question",
            width: 280,
            render: (_, content) => <Text strong>{readQuestion(content)}</Text>
        },
        {
            title: "答案",
            key: "answer",
            render: (_, content) => <Text type="secondary">{content.answer || "暂无答案"}</Text>
        },
        {
            key: "actions",
            options: (content) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑内容 ${readQuestion(content)}`,
                    onClick: () => onEdit(content)
                },
                {
                    danger: true,
                    key: "delete",
                    text: "删除",
                    ariaLabel: `删除内容 ${readQuestion(content)}`,
                    onClick: () => onDelete(content)
                }
            ]
        }
    ];

    return (
        <KuzhambuTable
            ariaLabel="三才图会内容表格"
            columns={columns}
            dataSource={contents}
            pagination={false}
            rowKey="id"
            size="middle"
            scroll={{ x: 720 }}
            sortable
            onSort={onSort}
        />
    );
};
