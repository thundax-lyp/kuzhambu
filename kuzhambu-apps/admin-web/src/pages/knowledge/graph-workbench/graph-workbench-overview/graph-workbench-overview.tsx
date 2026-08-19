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
    const metrics = [
        ["正式节点", overview?.publishedNodeCount],
        ["关系", overview?.publishedEdgeCount],
        ["覆盖素材", overview?.coveredMaterialCount],
        ["孤立节点", overview?.isolatedNodeCount]
    ];
    return (
        <dl aria-live="polite" className="graph-workbench-overview">
            {metrics.map(([label, value]) => (
                <div className="graph-workbench-overview-card" key={label}>
                    <dt>{label}</dt>
                    <dd>{value ?? "-"}</dd>
                </div>
            ))}
        </dl>
    );
};
