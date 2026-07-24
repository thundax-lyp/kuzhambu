import { Table, Tag } from "antd";
import { KuzhambuSpaceCompact, KuzhambuButton } from "@/components";
import type { ColumnsType } from "antd/es/table";
import type { GraphVersionRecord } from "../graph-results-types";

interface GraphVersionTableProps {
    loading?: boolean;
    selectedVersionId?: number | null;
    versions: GraphVersionRecord[];
    onOpenDetail: (version: GraphVersionRecord) => void;
    onOpenResults: (version: GraphVersionRecord) => void;
}

const readStatusColor = (status?: string | null) => {
    switch (status) {
        case "APPLIED":
            return "green";
        case "FAILED":
            return "red";
        case "SUCCEEDED":
            return "blue";
        default:
            return "default";
    }
};

export const GraphVersionTable = ({
    loading = false,
    selectedVersionId = null,
    versions,
    onOpenDetail,
    onOpenResults
}: GraphVersionTableProps) => {
    const columns: ColumnsType<GraphVersionRecord> = [
        {
            dataIndex: "versionId",
            key: "versionId",
            title: "版本号"
        },
        {
            dataIndex: "taskType",
            key: "taskType",
            title: "任务类型"
        },
        {
            dataIndex: "status",
            key: "status",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            ),
            title: "状态"
        },
        {
            dataIndex: "sourceContentType",
            key: "sourceContentType",
            title: "来源类型"
        },
        {
            dataIndex: "sourceContentId",
            key: "sourceContentId",
            title: "来源 ID"
        },
        {
            dataIndex: "versionNo",
            key: "versionNo",
            title: "版本序号"
        },
        {
            dataIndex: "refinementApplied",
            key: "refinementApplied",
            render: (refinementApplied?: boolean | null) =>
                refinementApplied ? <Tag color="gold">已精修</Tag> : <Tag>未精修</Tag>,
            title: "精修状态"
        },
        {
            key: "actions",
            render: (_, version) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="knowledge-graph-results-graph-version-view-detail-button"
                        onClick={() => onOpenDetail(version)}
                    >
                        查看详情
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-graph-results-graph-version-action-button"
                        type={selectedVersionId === version.versionId ? "primary" : "default"}
                        onClick={() => onOpenResults(version)}
                    >
                        查看正式结果
                    </KuzhambuButton>
                </KuzhambuSpaceCompact>
            ),
            title: "操作"
        }
    ];

    return (
        <Table<GraphVersionRecord>
            aria-label="知识图谱版本表格"
            columns={columns}
            dataSource={versions}
            loading={loading}
            pagination={false}
            rowKey={(version) => version.versionId}
        />
    );
};
