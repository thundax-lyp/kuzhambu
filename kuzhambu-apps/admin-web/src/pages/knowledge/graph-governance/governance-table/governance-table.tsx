import { KuzhambuButton, KuzhambuTable } from "@/components";
import type {
    GraphGovernanceNodeRecord,
    GraphGovernanceRelationRecord
} from "../graph-governance-types";

interface GovernanceTableProps {
    nodes: readonly GraphGovernanceNodeRecord[];
    relations: readonly GraphGovernanceRelationRecord[];
    onSelectNode: (node: GraphGovernanceNodeRecord) => void;
    onSelectRelation: (relation: GraphGovernanceRelationRecord) => void;
}

export const GovernanceTable = ({
    nodes,
    relations,
    onSelectNode,
    onSelectRelation
}: GovernanceTableProps) => (
    <>
        <KuzhambuTable<GraphGovernanceNodeRecord>
            ariaLabel="图谱治理节点表格"
            columns={[
                { dataIndex: "name", key: "name", title: "节点" },
                { dataIndex: "type", key: "type", title: "类型" },
                { dataIndex: "sourceCount", key: "sourceCount", title: "来源数" },
                {
                    key: "actions",
                    options: (node) => [
                        {
                            key: "select-node",
                            text: "查看节点",
                            testId: `knowledge-graph-governance-select-node-${node.id}-button`,
                            onClick: () => onSelectNode(node)
                        }
                    ],
                    title: "操作"
                }
            ]}
            dataSource={[...nodes]}
            pagination={false}
            rowKey="id"
        />
        <KuzhambuTable<GraphGovernanceRelationRecord>
            ariaLabel="图谱治理关系表格"
            columns={[
                { dataIndex: "type", key: "type", title: "关系" },
                { dataIndex: "sourceId", key: "sourceId", title: "来源节点" },
                { dataIndex: "targetId", key: "targetId", title: "目标节点" },
                {
                    key: "actions",
                    options: (relation) => [
                        {
                            key: "select-relation",
                            text: "查看关系",
                            testId: `knowledge-graph-governance-select-relation-${relation.id}-button`,
                            onClick: () => onSelectRelation(relation)
                        }
                    ],
                    title: "操作"
                }
            ]}
            dataSource={[...relations]}
            pagination={false}
            rowKey="id"
        />
    </>
);
