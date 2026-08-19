import { useCallback, useEffect, useRef, useState } from "react";
import * as service from "../graph-workbench-service";
import type {
    GraphPublishedEdgeRecord,
    GraphPublishedNodeRecord,
    GraphWorkbenchGraphRecord,
    GraphWorkbenchOverviewRecord
} from "../graph-workbench-types";

const GRAPH_EDGE_LIMIT = 600;
const GRAPH_EDGE_BATCH_SIZE = 24;
const GRAPH_BATCH_DELAY_MS = 0;
const PAGE_RANK_DAMPING = 0.85;
const PAGE_RANK_ITERATIONS = 24;

const logProjection = (event: string, details: Record<string, number>) => {
    if (import.meta.env.DEV) console.info(`[graph-workbench] ${event} ${JSON.stringify(details)}`);
};

export type GraphWorkbenchLoadState = "idle" | "loading" | "ready" | "unavailable" | "error";

const emptyGraph = (): GraphWorkbenchGraphRecord => ({ edges: [], nodes: [] });

const mergeGraph = (current: GraphWorkbenchGraphRecord, incoming: GraphWorkbenchGraphRecord) => {
    const nodesById = new Map<string, GraphPublishedNodeRecord>();
    current.nodes.forEach((node) => nodesById.set(node.id, node));
    incoming.nodes.forEach((node) => nodesById.set(node.id, node));
    const edgesById = new Map<string, GraphPublishedEdgeRecord>();
    current.edges.forEach((edge) => edgesById.set(edge.id, edge));
    incoming.edges.forEach((edge) => {
        if (
            edgesById.size < GRAPH_EDGE_LIMIT &&
            edge.sourceNodeId &&
            edge.targetNodeId &&
            nodesById.has(edge.sourceNodeId) &&
            nodesById.has(edge.targetNodeId)
        ) {
            edgesById.set(edge.id, edge);
        }
    });
    const acceptedEdges = [...edgesById.values()].slice(0, GRAPH_EDGE_LIMIT);
    const endpointIds = new Set(
        acceptedEdges
            .flatMap((edge) => [edge.sourceNodeId, edge.targetNodeId])
            .filter((id): id is string => Boolean(id))
    );
    return {
        edges: acceptedEdges,
        nodes: [...endpointIds]
            .map((id) => nodesById.get(id))
            .filter((node): node is GraphPublishedNodeRecord => Boolean(node))
    };
};

const scoreGraphNodes = (graph: GraphWorkbenchGraphRecord) => {
    const nodeIds = graph.nodes.map((node) => node.id);
    const neighbours = new Map(nodeIds.map((nodeId) => [nodeId, new Set<string>()]));
    graph.edges.forEach((edge) => {
        if (!edge.sourceNodeId || !edge.targetNodeId || edge.sourceNodeId === edge.targetNodeId)
            return;
        neighbours.get(edge.sourceNodeId)?.add(edge.targetNodeId);
        neighbours.get(edge.targetNodeId)?.add(edge.sourceNodeId);
    });
    if (nodeIds.length === 0) return new Map<string, number>();

    let scores = new Map(nodeIds.map((nodeId) => [nodeId, 1 / nodeIds.length]));
    for (let iteration = 0; iteration < PAGE_RANK_ITERATIONS; iteration += 1) {
        const next = new Map(
            nodeIds.map((nodeId) => [nodeId, (1 - PAGE_RANK_DAMPING) / nodeIds.length])
        );
        nodeIds.forEach((nodeId) => {
            const neighboursOfNode = neighbours.get(nodeId);
            if (!neighboursOfNode?.size) return;
            const contribution = (scores.get(nodeId) ?? 0) / neighboursOfNode.size;
            neighboursOfNode.forEach((neighbourId) => {
                next.set(
                    neighbourId,
                    (next.get(neighbourId) ?? 0) + PAGE_RANK_DAMPING * contribution
                );
            });
        });
        scores = next;
    }
    return scores;
};

export const prioritizeGraphForProjection = (
    graph: GraphWorkbenchGraphRecord,
    projectedNodeIds = new Set<string>()
): GraphWorkbenchGraphRecord => {
    const scores = scoreGraphNodes(graph);
    return {
        nodes: graph.nodes,
        edges: [...graph.edges].sort((left, right) => {
            const leftProjectionConnections = [left.sourceNodeId, left.targetNodeId].filter(
                (nodeId) => projectedNodeIds.has(nodeId ?? "")
            ).length;
            const rightProjectionConnections = [right.sourceNodeId, right.targetNodeId].filter(
                (nodeId) => projectedNodeIds.has(nodeId ?? "")
            ).length;
            const leftScore =
                (scores.get(left.sourceNodeId ?? "") ?? 0) +
                (scores.get(left.targetNodeId ?? "") ?? 0);
            const rightScore =
                (scores.get(right.sourceNodeId ?? "") ?? 0) +
                (scores.get(right.targetNodeId ?? "") ?? 0);
            return (
                rightProjectionConnections - leftProjectionConnections ||
                rightScore - leftScore ||
                left.id.localeCompare(right.id)
            );
        })
    };
};

export const useGraphWorkbenchAtlas = (enabled: boolean) => {
    const [overview, setOverview] = useState<GraphWorkbenchOverviewRecord | null>(null);
    const [overviewState, setOverviewState] = useState<GraphWorkbenchLoadState>("idle");
    const [graph, setGraph] = useState<GraphWorkbenchGraphRecord>(emptyGraph);
    const [graphState, setGraphState] = useState<GraphWorkbenchLoadState>("idle");
    const sessionRef = useRef(0);
    const projectionTimerRef = useRef<number | null>(null);
    const graphLaidOutRef = useRef<() => void>(() => undefined);
    const onGraphLaidOut = useCallback(() => graphLaidOutRef.current(), []);

    useEffect(() => {
        if (!enabled) {
            return;
        }
        const session = ++sessionRef.current;
        const controller = new AbortController();
        const current = () => sessionRef.current === session && !controller.signal.aborted;
        queueMicrotask(() => {
            if (current()) {
                setOverviewState("loading");
                setGraphState("loading");
                setGraph(emptyGraph());
            }
        });

        void service.getWorkbenchOverview({ signal: controller.signal }).then(
            (value) => {
                if (current()) {
                    setOverview(value);
                    setOverviewState("ready");
                }
            },
            (error: unknown) => {
                if (current()) {
                    setOverviewState(
                        (error as { code?: string }).code === "WORKBENCH_SNAPSHOT_UNAVAILABLE"
                            ? "unavailable"
                            : "error"
                    );
                }
            }
        );

        void (async () => {
            try {
                const recent = await service.listRecentEdges({ signal: controller.signal });
                if (!current()) return;
                let accepted = emptyGraph();
                const projectedEdgeIds = new Set<string>();
                const projectedNodeIds = new Set<string>();
                let completedLoading = false;
                let awaitingRender = false;
                let projectionBatch = 0;
                const scheduleProjection = () => {
                    if (!current() || awaitingRender || projectionTimerRef.current !== null) return;
                    logProjection("next batch scheduled", {
                        delayMs: GRAPH_BATCH_DELAY_MS,
                        projectedEdgeCount: projectedEdgeIds.size
                    });
                    projectionTimerRef.current = window.setTimeout(() => {
                        projectionTimerRef.current = null;
                        const ordered = prioritizeGraphForProjection(accepted, projectedNodeIds);
                        const nextEdges = ordered.edges
                            .filter((edge) => !projectedEdgeIds.has(edge.id))
                            .slice(0, GRAPH_EDGE_BATCH_SIZE);
                        if (nextEdges.length === 0) {
                            if (completedLoading) setGraphState("ready");
                            return;
                        }
                        nextEdges.forEach((edge) => {
                            projectedEdgeIds.add(edge.id);
                            if (edge.sourceNodeId) projectedNodeIds.add(edge.sourceNodeId);
                            if (edge.targetNodeId) projectedNodeIds.add(edge.targetNodeId);
                        });
                        projectionBatch += 1;
                        logProjection("batch emitted", {
                            batch: projectionBatch,
                            edgeCount: projectedEdgeIds.size,
                            nodeCount: projectedNodeIds.size
                        });
                        setGraph(
                            mergeGraph(emptyGraph(), {
                                edges: ordered.edges.filter((edge) =>
                                    projectedEdgeIds.has(edge.id)
                                ),
                                nodes: accepted.nodes
                            })
                        );
                        awaitingRender = true;
                    }, GRAPH_BATCH_DELAY_MS);
                };
                graphLaidOutRef.current = () => {
                    if (!current() || !awaitingRender) return;
                    awaitingRender = false;
                    scheduleProjection();
                };
                const appendCandidateGraph = (incoming: GraphWorkbenchGraphRecord) => {
                    accepted = mergeGraph(accepted, incoming);
                    scheduleProjection();
                };

                appendCandidateGraph(recent);
                if (accepted.edges.length === 0) {
                    completedLoading = true;
                    setGraphState("ready");
                    return;
                }
                const nodeIds = accepted.nodes.map((node) => node.id);
                let afterEdgeId: string | null = null;
                let truncated = true;
                while (truncated && accepted.edges.length < GRAPH_EDGE_LIMIT && current()) {
                    const batch = await service.listOneHopEdges({
                        afterEdgeId,
                        nodeIds,
                        signal: controller.signal
                    });
                    if (!current()) return;
                    appendCandidateGraph(batch);
                    afterEdgeId = batch.nextCursor;
                    truncated = batch.truncated && Boolean(afterEdgeId);
                }
                if (!current()) return;
                completedLoading = true;
                scheduleProjection();
            } catch {
                if (current()) setGraphState("error");
            }
        })();
        return () => {
            controller.abort();
            graphLaidOutRef.current = () => undefined;
            if (projectionTimerRef.current !== null) {
                window.clearTimeout(projectionTimerRef.current);
                projectionTimerRef.current = null;
            }
        };
    }, [enabled]);

    return { graph, graphState, onGraphLaidOut, overview, overviewState };
};
