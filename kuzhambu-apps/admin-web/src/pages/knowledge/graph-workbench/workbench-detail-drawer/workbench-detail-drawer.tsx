import { Descriptions } from "antd";
import { useNavigate } from "react-router-dom";
import { KuzhambuButton, KuzhambuDrawer, KuzhambuSpace } from "@/components";
import type { GraphWorkbenchNodeRecord } from "../graph-workbench-types";

interface WorkbenchDetailDrawerProps {
    node?: GraphWorkbenchNodeRecord | null;
    onClose: () => void;
    open: boolean;
}

export const WorkbenchDetailDrawer = ({ node, onClose, open }: WorkbenchDetailDrawerProps) => {
    const navigate = useNavigate();
    return (
        <KuzhambuDrawer
            open={open}
            onClose={onClose}
            size="middle"
            testId="knowledge-graph-workbench-detail-drawer"
            title="图谱节点详情"
        >
            <Descriptions bordered column={1} size="small">
                <Descriptions.Item label="节点">{node?.label ?? "-"}</Descriptions.Item>
                <Descriptions.Item label="来源素材">{node?.sourceName ?? "-"}</Descriptions.Item>
                <Descriptions.Item label="质量待办">{node?.qualityTodo ?? "-"}</Descriptions.Item>
            </Descriptions>
            <KuzhambuSpace style={{ marginTop: 16 }}>
                <KuzhambuButton
                    testId="knowledge-graph-workbench-open-governance-button"
                    onClick={() => navigate(`/knowledge/graph-governance?nodeId=${node?.id ?? ""}`)}
                >
                    跳转治理
                </KuzhambuButton>
                <KuzhambuButton
                    testId="knowledge-graph-workbench-open-material-button"
                    onClick={() =>
                        navigate(`/knowledge/graph-material?source=${node?.sourceName ?? ""}`)
                    }
                >
                    查看素材
                </KuzhambuButton>
            </KuzhambuSpace>
        </KuzhambuDrawer>
    );
};
