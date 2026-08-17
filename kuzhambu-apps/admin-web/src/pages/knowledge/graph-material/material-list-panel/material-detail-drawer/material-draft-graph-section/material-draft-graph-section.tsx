import { Empty } from "antd";
import { hasPermission } from "@/auth/permission-storage";
import type { GraphMaterialDetailRecord } from "@/pages/knowledge/graph-material/graph-material-types";
import { MaterialDraftCanvas } from "@/pages/knowledge/graph-material/material-draft-canvas";
import "./material-draft-graph-section.css";

interface MaterialDraftGraphSectionProps {
    detail: GraphMaterialDetailRecord | null;
}

export const MaterialDraftGraphSection = ({ detail }: MaterialDraftGraphSectionProps) => {
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const material = detail?.material ?? null;

    return (
        <div
            className="knowledge-graph-material-draft-graph-section"
            data-testid="knowledge-graph-material-detail-draft-graph-section"
        >
            {material ? (
                <MaterialDraftCanvas
                    canApplyGraph={canEditGraph}
                    canEditGraph={canEditGraph}
                    detail={detail}
                    material={material}
                />
            ) : (
                <Empty
                    data-testid="knowledge-graph-material-detail-draft-graph-empty"
                    description="素材尚未初始化，暂无草稿图谱。"
                />
            )}
        </div>
    );
};
