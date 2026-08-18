import { Empty } from "antd";
import { hasPermission } from "@/auth/permission-storage";
import type { GraphMaterialDetailRecord } from "@/pages/knowledge/graph-material/graph-material-types";
import { MaterialDraftCanvas } from "@/pages/knowledge/graph-material/material-draft-canvas";
import "./material-draft-graph-section.css";

interface MaterialDraftGraphSectionProps {
    detail: GraphMaterialDetailRecord | null;
    onRefresh: () => Promise<unknown>;
}

export const MaterialDraftGraphSection = ({
    detail,
    onRefresh
}: MaterialDraftGraphSectionProps) => {
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const material = detail?.material ?? null;

    return (
        <div
            className="knowledge-graph-material-draft-graph-section"
            data-testid="knowledge-graph-material-detail-draft-graph-section"
        >
            {material ? (
                <MaterialDraftCanvas
                    canEditGraph={canEditGraph}
                    detail={detail}
                    material={material}
                    onRefresh={onRefresh}
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
