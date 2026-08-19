import { Graph, GraphEvent } from "@antv/g6";
import type { IElementDragEvent, IGraphLifeCycleEvent } from "@antv/g6";
import { useEffect, useRef } from "react";
import type { GraphWorkbenchGraphRecord } from "../graph-workbench-types";
import "./graph-workbench-canvas.css";

export interface GraphWorkbenchCanvasProps {
    graph: GraphWorkbenchGraphRecord;
    motion: boolean;
    onProjectionLaidOut: () => void;
    projecting: boolean;
}

const INITIAL_LAYOUT_ITERATIONS = 120;
const INCREMENTAL_LAYOUT_ITERATIONS = 30;
const FINAL_LAYOUT_ITERATIONS = 120;
const INITIAL_LAYOUT_ALPHA_DECAY = 0.055;
const INCREMENTAL_LAYOUT_ALPHA_DECAY = 0.2;
const FINAL_LAYOUT_ALPHA_DECAY = 0.055;
const INITIAL_FIT_ZOOM_OUT_FACTOR = 3;

const forceLayout = (animate: boolean, alphaDecay: number, iterations: number) => ({
    alphaDecay,
    animation: animate,
    collide: { radius: 34, strength: 0.9 },
    iterations,
    link: { distance: 180, strength: 0.78 },
    manyBody: { distanceMax: 520, strength: -260 },
    type: "d3-force" as const
});

const graphNodeId = (id: string) => `node:${id}`;
const graphEdgeId = (id: string) => `edge:${id}`;

const logProjection = (event: string, details: Record<string, number>) => {
    if (import.meta.env.DEV) console.info(`[graph-workbench] ${event} ${JSON.stringify(details)}`);
};

const nodeStyle = (nodeType?: string | null) => {
    const styles: Record<string, { fill: string; stroke: string; type: string }> = {
        PERSON: { fill: "#D2A445", stroke: "#8C6516", type: "circle" },
        PLACE: { fill: "#2B9A92", stroke: "#16645F", type: "rect" },
        BUILDING: { fill: "#597DBA", stroke: "#344E7B", type: "rect" },
        WORK: { fill: "#8A6AC0", stroke: "#5B4385", type: "diamond" },
        EVENT: { fill: "#CA7259", stroke: "#8A4532", type: "hexagon" }
    };
    return styles[nodeType || ""] || { fill: "#718096", stroke: "#465467", type: "circle" };
};

export const GraphWorkbenchCanvas = ({
    graph,
    motion,
    onProjectionLaidOut,
    projecting
}: GraphWorkbenchCanvasProps) => {
    const containerRef = useRef<HTMLDivElement | null>(null);
    const graphRef = useRef<Graph | null>(null);
    const renderQueueRef = useRef(Promise.resolve());
    const hasCenteredInitialProjectionRef = useRef(false);
    const lastLayoutAtRef = useRef<number | null>(null);
    const projectionStartedAtRef = useRef<number | null>(null);
    const projectedNodeIdsRef = useRef(new Set<string>());

    const hasGraph = graph.edges.length > 0;

    useEffect(() => {
        const container = containerRef.current;
        if (!container || !hasGraph) return undefined;
        const instance = new Graph({
            animation: false,
            container,
            data: { edges: [], nodes: [] },
            height: container.clientHeight || 560,
            node: { style: { labelText: (datum) => String(datum.data?.label || "") } },
            behaviors: [
                "drag-canvas",
                "zoom-canvas",
                { type: "fix-element-size", enable: true },
                {
                    type: "drag-element-force",
                    fixed: false,
                    hideEdge: "none",
                    enable: (event: IElementDragEvent) => event.targetType === "node"
                }
            ],
            width: container.clientWidth || 960
        });
        graphRef.current = instance;
        renderQueueRef.current = Promise.resolve();
        hasCenteredInitialProjectionRef.current = false;
        lastLayoutAtRef.current = null;
        projectionStartedAtRef.current = null;
        projectedNodeIdsRef.current.clear();
        const observer = new ResizeObserver(() =>
            instance.resize(container.clientWidth, container.clientHeight)
        );
        observer.observe(container);
        return () => {
            observer.disconnect();
            graphRef.current = null;
            void renderQueueRef.current.finally(() => instance.destroy());
        };
    }, [hasGraph]);

    useEffect(() => {
        const instance = graphRef.current;
        if (!instance) return;
        const nextData = {
            edges: graph.edges
                .filter(
                    (edge): edge is typeof edge & { sourceNodeId: string; targetNodeId: string } =>
                        Boolean(edge.sourceNodeId && edge.targetNodeId)
                )
                .map((edge) => ({
                    id: graphEdgeId(edge.id),
                    source: graphNodeId(edge.sourceNodeId),
                    style: { stroke: "#8493A8" },
                    target: graphNodeId(edge.targetNodeId)
                })),
            nodes: graph.nodes.map((node, index) => {
                const angle = index * 2.399963229728653;
                const radius = 44 + Math.sqrt(index) * 20;
                const style = nodeStyle(node.nodeType);
                const previousPosition = projectedNodeIdsRef.current.has(node.id)
                    ? instance.getElementPosition(graphNodeId(node.id))
                    : [Math.cos(angle) * radius, Math.sin(angle) * radius];
                return {
                    data: { label: node.name && node.name.length <= 12 ? node.name : "" },
                    id: graphNodeId(node.id),
                    style: { fill: style.fill, lineWidth: 1.5, size: 12, stroke: style.stroke },
                    type: style.type,
                    x: previousPosition[0],
                    y: previousPosition[1]
                };
            })
        };
        renderQueueRef.current = renderQueueRef.current
            .then(async () => {
                const projectionStartedAt = performance.now();
                const isInitialProjection = !hasCenteredInitialProjectionRef.current;
                const layoutIterations = isInitialProjection
                    ? INITIAL_LAYOUT_ITERATIONS
                    : INCREMENTAL_LAYOUT_ITERATIONS;
                const layoutAlphaDecay = isInitialProjection
                    ? INITIAL_LAYOUT_ALPHA_DECAY
                    : INCREMENTAL_LAYOUT_ALPHA_DECAY;
                projectionStartedAtRef.current = projectionStartedAt;
                logProjection("projection started", {
                    edgeCount: graph.edges.length,
                    layoutAlphaDecay,
                    layoutIterations,
                    nodeCount: graph.nodes.length
                });
                let projectionLaidOut = false;
                const notifyProjectionLaidOut = () => {
                    if (projectionLaidOut) return;
                    projectionLaidOut = true;
                    instance.off(GraphEvent.AFTER_LAYOUT, onAfterLayout);
                    const layoutCompletedAt = performance.now();
                    logProjection("layout completed", {
                        edgeCount: graph.edges.length,
                        intervalSincePreviousLayoutMs: Math.round(
                            lastLayoutAtRef.current === null
                                ? 0
                                : layoutCompletedAt - lastLayoutAtRef.current
                        ),
                        layoutDurationMs: Math.round(
                            layoutCompletedAt -
                                (projectionStartedAtRef.current ?? layoutCompletedAt)
                        ),
                        layoutAlphaDecay,
                        layoutIterations,
                        nodeCount: graph.nodes.length
                    });
                    lastLayoutAtRef.current = layoutCompletedAt;
                    if (isInitialProjection) {
                        hasCenteredInitialProjectionRef.current = true;
                        const [width, height] = instance.getSize();
                        void instance
                            .fitView()
                            .then(() =>
                                instance.zoomTo(
                                    instance.getZoom() / INITIAL_FIT_ZOOM_OUT_FACTOR,
                                    motion ? { duration: 520, easing: "ease-in-out" } : undefined,
                                    [width / 2, height / 2]
                                )
                            )
                            .finally(onProjectionLaidOut);
                        return;
                    }
                    onProjectionLaidOut();
                };
                const onAfterLayout = (event: IGraphLifeCycleEvent) => {
                    if (event.data?.type === "post") notifyProjectionLaidOut();
                };
                instance.on(GraphEvent.AFTER_LAYOUT, onAfterLayout);
                instance.setOptions({
                    animation: motion ? { duration: 260, easing: "ease-out" } : false
                });
                instance.setData(nextData);
                projectedNodeIdsRef.current = new Set(graph.nodes.map((node) => node.id));
                instance.setLayout(forceLayout(motion, layoutAlphaDecay, layoutIterations));
                await instance.render().catch(notifyProjectionLaidOut);
                logProjection("projection render settled", {
                    edgeCount: graph.edges.length,
                    layoutAlphaDecay,
                    layoutIterations,
                    renderDurationMs: Math.round(performance.now() - projectionStartedAt),
                    nodeCount: graph.nodes.length
                });
                instance.setOptions({ animation: false });
            })
            .catch(() => undefined);
    }, [graph, motion, onProjectionLaidOut]);

    useEffect(() => {
        const instance = graphRef.current;
        if (!instance || !hasGraph || projecting) return;
        renderQueueRef.current = renderQueueRef.current
            .then(async () => {
                instance.setOptions({
                    animation: motion ? { duration: 640, easing: "ease-in-out" } : false
                });
                instance.setLayout(
                    forceLayout(motion, FINAL_LAYOUT_ALPHA_DECAY, FINAL_LAYOUT_ITERATIONS)
                );
                await instance.render();
                await instance.fitView(
                    undefined,
                    motion ? { duration: 520, easing: "ease-in-out" } : undefined
                );
                instance.setOptions({ animation: false });
            })
            .catch(() => undefined);
    }, [hasGraph, motion, projecting]);

    return (
        <div
            aria-label={
                hasGraph
                    ? `正式图画布：已展示 ${graph.nodes.length} 个节点和 ${graph.edges.length} 条关系`
                    : "正式图画布：正在准备首批关系"
            }
            className={`graph-workbench-canvas${hasGraph ? "" : " graph-workbench-canvas--idle"}`}
            ref={containerRef}
            role="img"
        >
            {hasGraph ? null : <span>图谱脉冲准备中</span>}
        </div>
    );
};
