import { useMemo } from "react";
import { KnowledgeGraphCanvas } from "@/components/knowledge-graph-canvas";
import { layoutAtlasForceGraph } from "./atlas-force-layout";
import type { AtlasGraphRecord } from "./atlas-workbench-types";

import "./atlas-workbench-canvas.css";

const createCanvasProjection = (graph: AtlasGraphRecord) => {
    const layout = layoutAtlasForceGraph(graph);
    return {
        graph: {
            edges: layout.edges.map((edge) => ({ ...edge, label: edge.relationLabel })),
            nodes: layout.nodes.map(({ node }) => node)
        },
        positions: new Map(layout.nodes.map(({ id, position }) => [id, position]))
    };
};

export const AtlasWorkbenchCanvas = ({
    onNodeExpand,
    graph,
    loading
}: {
    onNodeExpand: (nodeId: string) => void;
    graph: AtlasGraphRecord;
    loading: boolean;
}) => {
    const projection = useMemo(() => createCanvasProjection(graph), [graph]);
    if (graph.edges.length === 0 && !loading)
        return <p className="atlas-workbench-empty">暂无可展示的关系图谱。</p>;
    return (
        <section aria-label="三才图会总谱预览" className="atlas-workbench-canvas">
            <KnowledgeGraphCanvas
                ariaLabel="三才图会总谱画布"
                graph={projection.graph}
                nodePositions={projection.positions}
                onNodeDoubleClick={onNodeExpand}
            />
            {loading ? <p className="atlas-workbench-loading">正在扩展总谱关系…</p> : null}
        </section>
    );
};
