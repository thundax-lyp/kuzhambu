import { Descriptions } from "antd";
import { KuzhambuDrawer } from "@/components";
import type {
    GraphGovernanceNodeRecord,
    GraphGovernanceRelationRecord
} from "../graph-governance-types";

interface GovernanceDetailDrawerProps {
    node?: GraphGovernanceNodeRecord | null;
    onClose: () => void;
    open: boolean;
    relation?: GraphGovernanceRelationRecord | null;
}

export const GovernanceDetailDrawer = ({
    node,
    onClose,
    open,
    relation
}: GovernanceDetailDrawerProps) => (
    <KuzhambuDrawer
        open={open}
        onClose={onClose}
        title="治理来源与审计详情"
        size="middle"
        testId="knowledge-graph-governance-detail-drawer"
    >
        <Descriptions bordered column={1} size="small">
            <Descriptions.Item label="对象">
                {node?.name ?? relation?.type ?? "-"}
            </Descriptions.Item>
            <Descriptions.Item label="来源">
                {node ? `${node.sourceCount} 份素材` : "正式图谱关系"}
            </Descriptions.Item>
            <Descriptions.Item label="审计">Mock：最近一次治理浏览</Descriptions.Item>
        </Descriptions>
    </KuzhambuDrawer>
);
