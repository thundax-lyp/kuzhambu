import { Empty, Table } from "antd";
import type { ColumnsType } from "antd/es/table";
import { KuzhambuCard, KuzhambuSpace } from "@/components";
import { normalizeId } from "@/types/id";
import type { GraphWorkbenchRelationRecord } from "../graph-extraction-types";

interface CurrentGraphPanelProps {
    loading?: boolean;
    relations: GraphWorkbenchRelationRecord[];
    versionId?: string | null;
}

interface CurrentGraphCanvasItem {
    name: string;
    x: number;
    y: number;
}

const GRAPH_WIDTH = 520;
const GRAPH_HEIGHT = 220;
const GRAPH_CENTER_X = GRAPH_WIDTH / 2;
const GRAPH_CENTER_Y = GRAPH_HEIGHT / 2;
const GRAPH_RADIUS_X = 190;
const GRAPH_RADIUS_Y = 72;

const toGraphNodes = (relations: GraphWorkbenchRelationRecord[]) => {
    const names = Array.from(
        new Set(
            relations.flatMap((relation) =>
                [relation.sourceName, relation.targetName].filter(Boolean)
            ) as string[]
        )
    );

    return names.map<CurrentGraphCanvasItem>((name, index) => {
        const angle = (2 * Math.PI * index) / Math.max(names.length, 1);
        return {
            name,
            x: GRAPH_CENTER_X + Math.cos(angle) * GRAPH_RADIUS_X,
            y: GRAPH_CENTER_Y + Math.sin(angle) * GRAPH_RADIUS_Y
        };
    });
};

export const CurrentGraphPanel = ({
    loading = false,
    relations,
    versionId
}: CurrentGraphPanelProps) => {
    const nodes = toGraphNodes(relations);
    const nodesByName = new Map(nodes.map((node) => [node.name, node]));
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
                    <svg
                        aria-label="当前图谱关系图"
                        role="img"
                        viewBox={`0 0 ${GRAPH_WIDTH} ${GRAPH_HEIGHT}`}
                        style={{ width: "100%", minHeight: 220 }}
                    >
                        <defs>
                            <marker
                                id="current-graph-arrow"
                                markerHeight="8"
                                markerWidth="8"
                                orient="auto"
                                refX="7"
                                refY="4"
                            >
                                <path d="M0,0 L8,4 L0,8 Z" fill="#8c8c8c" />
                            </marker>
                        </defs>
                        {relations.map((relation) => {
                            const source = relation.sourceName
                                ? nodesByName.get(relation.sourceName)
                                : undefined;
                            const target = relation.targetName
                                ? nodesByName.get(relation.targetName)
                                : undefined;
                            if (!source || !target) {
                                return null;
                            }
                            const key = normalizeId(relation.relationId);
                            const labelX = (source.x + target.x) / 2;
                            const labelY = (source.y + target.y) / 2 - 8;
                            return (
                                <g key={key}>
                                    <line
                                        markerEnd="url(#current-graph-arrow)"
                                        stroke="#8c8c8c"
                                        strokeWidth="1.5"
                                        x1={source.x}
                                        x2={target.x}
                                        y1={source.y}
                                        y2={target.y}
                                    />
                                    <text
                                        fill="#595959"
                                        fontSize="12"
                                        textAnchor="middle"
                                        x={labelX}
                                        y={labelY}
                                    >
                                        {relation.relationType || "关系"}
                                    </text>
                                </g>
                            );
                        })}
                        {nodes.map((node) => (
                            <g key={node.name}>
                                <circle
                                    cx={node.x}
                                    cy={node.y}
                                    fill="#f0f5ff"
                                    r="28"
                                    stroke="#597ef7"
                                />
                                <text
                                    fill="#1f1f1f"
                                    fontSize="12"
                                    textAnchor="middle"
                                    x={node.x}
                                    y={node.y + 4}
                                >
                                    {node.name}
                                </text>
                            </g>
                        ))}
                    </svg>
                </KuzhambuSpace>
            ) : (
                <Empty description="暂无当前图谱" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
        </KuzhambuCard>
    );
};
