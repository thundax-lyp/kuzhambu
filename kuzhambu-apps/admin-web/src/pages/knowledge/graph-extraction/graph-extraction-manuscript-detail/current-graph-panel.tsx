import { Empty, Table } from "antd";
import type { ColumnsType } from "antd/es/table";
import { KuzhambuCard, KuzhambuGraph, KuzhambuSpace } from "@/components";
import { normalizeId } from "@/types/id";
import type { GraphWorkbenchRelationRecord } from "../graph-extraction-types";

interface CurrentGraphPanelProps {
    loading?: boolean;
    relations: GraphWorkbenchRelationRecord[];
    versionId?: string | null;
}

const toGraphSpoList = (relations: GraphWorkbenchRelationRecord[]) =>
    relations
        .filter((relation) => relation.sourceName && relation.relationType && relation.targetName)
        .map((relation) => ({
            subject: relation.sourceName || "",
            predicate: relation.relationType || "",
            object: relation.targetName || ""
        }));

export const CurrentGraphPanel = ({
    loading = false,
    relations,
    versionId
}: CurrentGraphPanelProps) => {
    const graphSpoList = toGraphSpoList(relations);
    const columns: ColumnsType<GraphWorkbenchRelationRecord> = [
        { dataIndex: "sourceName", key: "sourceName", title: "主语" },
        { dataIndex: "relationType", key: "relationType", title: "谓语" },
        { dataIndex: "targetName", key: "targetName", title: "宾语" }
    ];

    return (
        <KuzhambuCard
            className="graph-extraction-create-card"
            loading={loading}
            title={versionId ? `当前图谱 #${versionId}` : "当前图谱"}
            variant="borderless"
        >
            {versionId && relations.length > 0 ? (
                <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                    <Table<GraphWorkbenchRelationRecord>
                        aria-label="当前图谱 SPO 列表"
                        columns={columns}
                        dataSource={relations}
                        pagination={false}
                        rowKey={(relation) => normalizeId(relation.relationId)}
                        size="small"
                    />
                    <KuzhambuGraph spoList={graphSpoList} height={280} />
                </KuzhambuSpace>
            ) : (
                <Empty description="暂无当前图谱" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
        </KuzhambuCard>
    );
};
