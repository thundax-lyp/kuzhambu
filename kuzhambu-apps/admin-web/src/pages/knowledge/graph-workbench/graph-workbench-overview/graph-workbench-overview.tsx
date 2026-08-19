import type { GraphWorkbenchLoadState } from "../hooks/use-graph-workbench-atlas";
import type { GraphWorkbenchOverviewRecord } from "../graph-workbench-types";
import "./graph-workbench-overview.css";

export interface GraphWorkbenchOverviewProps {
    overview: GraphWorkbenchOverviewRecord | null;
    state: GraphWorkbenchLoadState;
}

export const GraphWorkbenchOverview = ({ overview, state }: GraphWorkbenchOverviewProps) => {
    if (state === "unavailable") {
        return <div className="graph-workbench-overview">正式图态势正在准备</div>;
    }
    return (
        <div aria-live="polite" className="graph-workbench-overview">
            <strong>正式节点 {overview?.publishedNodeCount ?? "-"}</strong>
            <span>关系 {overview?.publishedEdgeCount ?? "-"}</span>
            <span>覆盖素材 {overview?.coveredMaterialCount ?? "-"}</span>
            <span>待决冲突 {overview?.pendingConflictCount ?? "-"}</span>
            <span>孤立节点 {overview?.isolatedNodeCount ?? "-"}</span>
            <span>结构缺口 {overview?.missingCoreRelationNodeCount ?? "-"}</span>
        </div>
    );
};
