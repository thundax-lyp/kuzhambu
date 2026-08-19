import { useEffect, useRef, useState } from "react";
import * as service from "../graph-workbench-service";
import type {
    GraphPublishedEdgeRecord,
    GraphPublishedNodeRecord,
    GraphWorkbenchGraphRecord,
    GraphWorkbenchOverviewRecord
} from "../graph-workbench-types";

const GRAPH_EDGE_LIMIT = 600;

type LoadState = "idle" | "loading" | "ready" | "unavailable" | "error";

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
        acceptedEdges.flatMap((edge) => [edge.sourceNodeId, edge.targetNodeId]).filter(Boolean)
    );
    return {
        edges: acceptedEdges,
        nodes: [...endpointIds]
            .map((id) => nodesById.get(id))
            .filter((node): node is GraphPublishedNodeRecord => Boolean(node))
    };
};

export const useGraphWorkbenchAtlas = (enabled: boolean) => {
    const [overview, setOverview] = useState<GraphWorkbenchOverviewRecord | null>(null);
    const [overviewState, setOverviewState] = useState<LoadState>("idle");
    const [graph, setGraph] = useState<GraphWorkbenchGraphRecord>(emptyGraph);
    const [graphState, setGraphState] = useState<LoadState>("idle");
    const sessionRef = useRef(0);

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
                let accepted = mergeGraph(emptyGraph(), recent);
                setGraph(accepted);
                if (accepted.edges.length === 0) {
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
                    accepted = mergeGraph(accepted, batch);
                    setGraph(accepted);
                    afterEdgeId = batch.nextCursor;
                    truncated = batch.truncated && Boolean(afterEdgeId);
                }
                if (current()) setGraphState("ready");
            } catch {
                if (current()) setGraphState("error");
            }
        })();
        return () => controller.abort();
    }, [enabled]);

    return { graph, graphState, overview, overviewState };
};
