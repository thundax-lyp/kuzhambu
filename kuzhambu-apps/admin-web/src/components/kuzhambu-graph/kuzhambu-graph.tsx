import { Graph, NodeEvent } from "@antv/g6";
import type { EdgeData, GraphData, IElementDragEvent, NodeData } from "@antv/g6";
import { forwardRef, useEffect, useImperativeHandle, useMemo, useRef } from "react";

import { buildKuzhambuGraphData, mergeKuzhambuGraphData } from "./kuzhambu-graph-data";
import "./kuzhambu-graph.css";
import type { KuzhambuGraphHandle, KuzhambuGraphSpoItem } from "./kuzhambu-graph-types";

const GROUP_COLORS = ["#1677ff", "#52c41a", "#faad14", "#722ed1", "#eb2f96", "#13c2c2"];
const FORCE_LINK_DISTANCE = 112;
const FORCE_LINK_STRENGTH = 0.82;
const FORCE_GROUP_MEMBER_LINK_DISTANCE = 76;
const FORCE_GROUP_MEMBER_LINK_STRENGTH = 0.38;
const FORCE_GROUP_LINK_DISTANCE = 220;
const FORCE_GROUP_LINK_STRENGTH = 0.24;
const FORCE_MANY_BODY_STRENGTH = -180;
const FORCE_MANY_BODY_DISTANCE_MAX = 280;
const FORCE_COLLIDE_RADIUS = 38;
const FORCE_COLLIDE_STRENGTH = 0.9;
const FORCE_CENTER_STRENGTH = 0.14;
const FORCE_LAYOUT_ITERATIONS = 280;
const FORCE_GROUP_Y_SPACING = 116;
const FORCE_GROUP_Y_MAX_SPAN = 360;
const FORCE_GROUP_Y_STRENGTH = 0.08;

const getNodeGroup = (node: NodeData) => {
    return String(node.data?.group || "default");
};

const getNodeLayoutGroup = (node: NodeData) => {
    return String(node.data?.layoutGroup || getNodeGroup(node));
};

const isVirtualNode = (node: NodeData) => Boolean(node.data?.isVirtual);

const getNodeLabel = (node: NodeData) => {
    return String(node.data?.label || node.id);
};

const isVirtualEdge = (edge: EdgeData) => Boolean(edge.data?.isVirtual);

const getEdgeLabel = (edge: EdgeData) => {
    return String(edge.data?.label || "");
};

const getEdgeLinkDistance = (edge: EdgeData) => {
    if (edge.data?.virtualType === "group-member") {
        return FORCE_GROUP_MEMBER_LINK_DISTANCE;
    }
    if (edge.data?.virtualType === "group-link") {
        return FORCE_GROUP_LINK_DISTANCE;
    }
    return FORCE_LINK_DISTANCE;
};

const getEdgeLinkStrength = (edge: EdgeData) => {
    if (edge.data?.virtualType === "group-member") {
        return FORCE_GROUP_MEMBER_LINK_STRENGTH;
    }
    if (edge.data?.virtualType === "group-link") {
        return FORCE_GROUP_LINK_STRENGTH;
    }
    return FORCE_LINK_STRENGTH;
};

const getGroupColor = (group: string) => {
    let hash = 0;
    Array.from(group).forEach((char) => {
        hash = (hash * 31 + char.charCodeAt(0)) % GROUP_COLORS.length;
    });
    return GROUP_COLORS[hash];
};

const getGraphGroups = (graphData: GraphData) => {
    return Array.from(
        new Set((graphData.nodes || []).map((node) => getNodeLayoutGroup(node)).filter(Boolean))
    );
};

const createGroupYResolver = (graphData: GraphData) => {
    const groups = getGraphGroups(graphData);
    const groupIndexMap = new Map(groups.map((group, index) => [group, index]));
    const span = Math.min(
        FORCE_GROUP_Y_SPACING * Math.max(groups.length - 1, 0),
        FORCE_GROUP_Y_MAX_SPAN
    );
    const startY = -span / 2;
    const stepY = groups.length <= 1 ? 0 : span / (groups.length - 1);

    return (node: NodeData) => startY + (groupIndexMap.get(getNodeLayoutGroup(node)) || 0) * stepY;
};

const createForceLayout = (graphData: GraphData) => {
    const groups = getGraphGroups(graphData);
    return {
        type: "d3-force",
        animation: true,
        iterations: FORCE_LAYOUT_ITERATIONS,
        link: {
            distance: getEdgeLinkDistance,
            strength: getEdgeLinkStrength
        },
        manyBody: {
            strength: FORCE_MANY_BODY_STRENGTH,
            distanceMax: FORCE_MANY_BODY_DISTANCE_MAX
        },
        collide: {
            radius: FORCE_COLLIDE_RADIUS,
            strength: FORCE_COLLIDE_STRENGTH
        },
        center: {
            strength: FORCE_CENTER_STRENGTH
        },
        y:
            groups.length > 1
                ? {
                      y: createGroupYResolver(graphData),
                      strength: FORCE_GROUP_Y_STRENGTH
                  }
                : false
    };
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
                    graph.setLayout(createForceLayout(nextData));
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
                layout: createForceLayout(initialGraphData),
                node: {
                    style: (node) => {
                        if (isVirtualNode(node)) {
                            return {
                                size: 1,
                                fill: "transparent",
                                stroke: "transparent",
                                opacity: 0,
                                visibility: "hidden",
                                lineWidth: 0,
                                labelText: ""
                            };
                        }
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
                    style: (edge) => {
                        if (isVirtualEdge(edge)) {
                            return {
                                stroke: "transparent",
                                opacity: 0,
                                visibility: "hidden",
                                lineWidth: 0,
                                endArrow: false,
                                labelText: ""
                            };
                        }
                        return {
                            stroke: "#94a3b8",
                            lineWidth: 1.2,
                            endArrow: true,
                            labelText: getEdgeLabel(edge),
                            labelFill: "#475569",
                            labelFontSize: 11,
                            labelBackground: true,
                            labelBackgroundFill: "rgba(255, 255, 255, 0.82)",
                            labelBackgroundRadius: 4
                        };
                    }
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
                graph.setLayout(createForceLayout(dataRef.current));
                await graph.render();
                await fitGraphView(graph);
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
            graph.setLayout(createForceLayout(graphData));
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
