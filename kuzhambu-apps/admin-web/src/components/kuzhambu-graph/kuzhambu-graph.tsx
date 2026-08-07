import { Graph, NodeEvent } from "@antv/g6";
import type { EdgeData, GraphData, IElementDragEvent, NodeData } from "@antv/g6";
import { forwardRef, useEffect, useImperativeHandle, useMemo, useRef } from "react";

import { buildKuzhambuGraphData, mergeKuzhambuGraphData } from "./kuzhambu-graph-data";
import "./kuzhambu-graph.css";
import type { KuzhambuGraphHandle, KuzhambuGraphSpoItem } from "./kuzhambu-graph-types";

const GROUP_COLORS = ["#1677ff", "#52c41a", "#faad14", "#722ed1", "#eb2f96", "#13c2c2"];

const getNodeGroup = (node: NodeData) => {
    return String(node.data?.group || "default");
};

const getNodeLabel = (node: NodeData) => {
    return String(node.data?.label || node.id);
};

const getEdgeLabel = (edge: EdgeData) => {
    return String(edge.data?.label || "");
};

const getGroupColor = (group: string) => {
    let hash = 0;
    Array.from(group).forEach((char) => {
        hash = (hash * 31 + char.charCodeAt(0)) % GROUP_COLORS.length;
    });
    return GROUP_COLORS[hash];
};

const fitGraphView = async (graph: Graph) => {
    await graph.fitView(
        {
            when: "always",
            direction: "both"
        },
        {
            duration: 520,
            easing: "ease-in-out"
        }
    );
};

export interface KuzhambuGraphProps {
    spoList: KuzhambuGraphSpoItem[];
    className?: string;
    height?: number;
}

export const KuzhambuGraph = forwardRef<KuzhambuGraphHandle, KuzhambuGraphProps>(
    ({ spoList, className, height = 520 }, ref) => {
        const containerRef = useRef<HTMLDivElement | null>(null);
        const graphRef = useRef<Graph | null>(null);
        const initialGraphDataRef = useRef<GraphData | null>(null);
        const dataRef = useRef<GraphData>({ nodes: [], edges: [] });
        const graphData = useMemo(() => buildKuzhambuGraphData(spoList), [spoList]);
        const classNames = ["kuzhambu-graph", className].filter(Boolean).join(" ");
        const hasSpoItems = spoList.length > 0;

        if (!initialGraphDataRef.current) {
            initialGraphDataRef.current = graphData;
        }

        useImperativeHandle(
            ref,
            () => ({
                appendSpoList: async (appendedSpoList) => {
                    const graph = graphRef.current;
                    if (!graph || appendedSpoList.length === 0) {
                        return;
                    }

                    const appendedData = buildKuzhambuGraphData(appendedSpoList);
                    const nextData = mergeKuzhambuGraphData(dataRef.current, appendedData);
                    dataRef.current = nextData;

                    graph.setData(nextData);
                    await graph.render();
                    await fitGraphView(graph);
                }
            }),
            []
        );

        useEffect(() => {
            const container = containerRef.current;
            if (!container) {
                return undefined;
            }

            const initialGraphData = initialGraphDataRef.current ?? { nodes: [], edges: [] };
            const graph = new Graph({
                container,
                autoFit: "view",
                animation: {
                    duration: 360,
                    easing: "ease-in-out"
                },
                data: initialGraphData,
                layout: {
                    type: "d3-force",
                    animation: true,
                    iterations: 260,
                    link: {
                        distance: 130,
                        strength: 0.75
                    },
                    manyBody: {
                        strength: -360
                    },
                    collide: {
                        radius: 44,
                        strength: 1
                    },
                    center: {
                        strength: 0.08
                    }
                },
                node: {
                    style: (node) => {
                        const color = getGroupColor(getNodeGroup(node));
                        return {
                            size: 38,
                            fill: "#ffffff",
                            stroke: color,
                            lineWidth: 2,
                            labelText: getNodeLabel(node),
                            labelFill: "#1f2937",
                            labelFontSize: 12,
                            labelPlacement: "bottom"
                        };
                    }
                },
                edge: {
                    style: (edge) => ({
                        stroke: "#94a3b8",
                        lineWidth: 1.2,
                        endArrow: true,
                        labelText: getEdgeLabel(edge),
                        labelFill: "#475569",
                        labelFontSize: 11,
                        labelBackground: true,
                        labelBackgroundFill: "rgba(255, 255, 255, 0.82)",
                        labelBackgroundRadius: 4
                    })
                },
                behaviors: [
                    "drag-canvas",
                    "zoom-canvas",
                    "optimize-viewport-transform",
                    {
                        type: "drag-element-force",
                        fixed: false,
                        hideEdge: "none",
                        enable: (event: IElementDragEvent) => event.targetType === "node"
                    }
                ]
            });

            const rerunForceLayout = async () => {
                graph.setLayout((layout) => ({
                    ...(Array.isArray(layout) ? layout[0] : layout),
                    type: "d3-force",
                    animation: true
                }));
                await graph.render();
            };

            graph.on(NodeEvent.DRAG_END, rerunForceLayout);
            graphRef.current = graph;
            dataRef.current = initialGraphData;

            void graph.render().then(() => fitGraphView(graph));

            const resizeObserver = new ResizeObserver(() => {
                graph.resize();
            });
            resizeObserver.observe(container);

            return () => {
                resizeObserver.disconnect();
                graph.off(NodeEvent.DRAG_END, rerunForceLayout);
                graph.destroy();
                graphRef.current = null;
            };
        }, []);

        useEffect(() => {
            const graph = graphRef.current;
            dataRef.current = graphData;
            if (!graph) {
                return;
            }

            graph.setData(graphData);
            void graph.render().then(() => fitGraphView(graph));
        }, [graphData]);

        return (
            <div className={classNames} style={{ height }}>
                <div ref={containerRef} className="kuzhambu-graph__canvas" />
                {!hasSpoItems && <div className="kuzhambu-graph__empty">暂无 SPO 关系</div>}
            </div>
        );
    }
);

KuzhambuGraph.displayName = "KuzhambuGraph";
