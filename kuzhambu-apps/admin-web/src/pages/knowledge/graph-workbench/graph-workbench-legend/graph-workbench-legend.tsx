import type { GraphWorkbenchGraphRecord } from "../graph-workbench-types";
import "./graph-workbench-legend.css";

export interface GraphWorkbenchLegendProps {
    graph: GraphWorkbenchGraphRecord;
}

export const GraphWorkbenchLegend = ({ graph }: GraphWorkbenchLegendProps) => (
    <div className="graph-workbench-legend">
        已展示 {graph.nodes.length} 个节点、{graph.edges.length} 条正式关系
        {graph.edges.length >= 600 ? "；已展示局部正式图" : "；关系箭头指向宾语"}
    </div>
);
