import { Graph } from "@antv/g6";
import { useEffect, useRef } from "react";
import type { GraphWorkbenchGraphRecord } from "../graph-workbench-types";
import "./graph-workbench-canvas.css";

export interface GraphWorkbenchCanvasProps {
    graph: GraphWorkbenchGraphRecord;
    motion: boolean;
}

const nodeStyle = (nodeType?: string | null) => {
    const styles: Record<string, { fill: string; type: string }> = {
        PERSON: { fill: "#D9B35A", type: "circle" },
        PLACE: { fill: "#4E9E91", type: "rect" },
        BUILDING: { fill: "#4E9E91", type: "rect" },
        WORK: { fill: "#8B78C8", type: "diamond" },
        EVENT: { fill: "#CF765B", type: "hexagon" }
    };
    return styles[nodeType || ""] || { fill: "#718096", type: "circle" };
};

export const GraphWorkbenchCanvas = ({ graph, motion }: GraphWorkbenchCanvasProps) => {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const graphRef = useRef<Graph | null>(null);
    const renderQueueRef = useRef(Promise.resolve());

    useEffect(() => {
        const container = containerRef.current;
        if (!container) return undefined;
        const instance = new Graph({
            animation: motion,
            autoFit: "view",
            container,
            data: { edges: [], nodes: [] },
            height: container.clientHeight || 560,
            layout: { type: "d3-force" },
            node: { style: { labelText: (datum) => String(datum.data?.label || "") } },
            width: container.clientWidth || 960
        });
        graphRef.current = instance;
        renderQueueRef.current = Promise.resolve();
        const observer = new ResizeObserver(() =>
            instance.resize(container.clientWidth, container.clientHeight)
        );
        observer.observe(container);
        return () => {
            observer.disconnect();
            graphRef.current = null;
            void renderQueueRef.current.finally(() => instance.destroy());
        };
    }, [motion]);

    useEffect(() => {
        const instance = graphRef.current;
        if (!instance) return;
        instance.setData({
            edges: graph.edges
                .filter(
                    (edge): edge is typeof edge & { sourceNodeId: string; targetNodeId: string } =>
                        Boolean(edge.sourceNodeId && edge.targetNodeId)
                )
                .map((edge) => ({
                    id: edge.id,
                    source: edge.sourceNodeId,
                    style: { endArrow: true, stroke: "#8493A8" },
                    target: edge.targetNodeId
                })),
            nodes: graph.nodes.map((node) => ({
                data: { label: node.name && node.name.length <= 12 ? node.name : "" },
                id: node.id,
                style: { fill: nodeStyle(node.nodeType).fill, size: 12 },
                type: nodeStyle(node.nodeType).type
            }))
        });
        renderQueueRef.current = renderQueueRef.current
            .then(() => instance.render())
            .catch(() => undefined);
    }, [graph]);

    return (
        <div
            aria-label={`正式图画布：已展示 ${graph.nodes.length} 个节点和 ${graph.edges.length} 条关系`}
            className="graph-workbench-canvas"
            ref={containerRef}
            role="img"
        />
    );
};
