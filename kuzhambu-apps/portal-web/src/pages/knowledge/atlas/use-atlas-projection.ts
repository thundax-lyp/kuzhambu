import { useEffect, useRef, useState } from "react";
import * as service from "./atlas-workbench-service";
import type {
    AtlasEdgeRecord,
    AtlasGraphRecord,
    AtlasNodeRecord,
    AtlasOverviewRecord
} from "./atlas-workbench-types";

const EMPTY_GRAPH: AtlasGraphRecord = { edges: [], nodes: [] };
const INITIAL_MAX_EDGES = 40;
const EXPANDED_MAX_EDGES = 160;

const mergeGraph = (
    current: AtlasGraphRecord,
    incoming: AtlasGraphRecord,
    maximumEdges: number
): AtlasGraphRecord => {
    const nodes = new Map<string, AtlasNodeRecord>();
    [...current.nodes, ...incoming.nodes].forEach((node) => nodes.set(node.id, node));
    const edges = new Map<string, AtlasEdgeRecord>();
    [...current.edges, ...incoming.edges].forEach((edge) => {
        if (
            edges.size < maximumEdges &&
            edge.sourceNodeId &&
            edge.targetNodeId &&
            nodes.has(edge.sourceNodeId) &&
            nodes.has(edge.targetNodeId)
        ) {
            edges.set(edge.id, edge);
        }
    });
    const acceptedEdges = [...edges.values()];
    const connectedNodeIds = new Set(
        acceptedEdges.flatMap((edge) => [edge.sourceNodeId, edge.targetNodeId])
    );
    return {
        edges: acceptedEdges,
        nodes: [...connectedNodeIds]
            .map((nodeId) => nodes.get(nodeId ?? ""))
            .filter((node): node is AtlasNodeRecord => Boolean(node))
    };
};

export const useAtlasProjection = () => {
    const [overview, setOverview] = useState<AtlasOverviewRecord | null>(null);
    const [graph, setGraph] = useState<AtlasGraphRecord>(EMPTY_GRAPH);
    const [graphState, setGraphState] = useState<"loading" | "ready" | "error">("loading");
    const graphRef = useRef(graph);
    const expandingNodeIdsRef = useRef(new Set<string>());

    const appendGraph = (incoming: AtlasGraphRecord, maximumEdges: number) => {
        const next = mergeGraph(graphRef.current, incoming, maximumEdges);
        graphRef.current = next;
        setGraph(next);
        return next;
    };

    useEffect(() => {
        let active = true;
        void service.getOverview().then(
            (value) => active && setOverview(value),
            () => active && setOverview(null)
        );
        void (async () => {
            try {
                let projection = mergeGraph(
                    EMPTY_GRAPH,
                    await service.listRecentEdges(),
                    INITIAL_MAX_EDGES
                );
                if (!active) return;
                graphRef.current = projection;
                setGraph(projection);
                let afterEdgeId: string | null = null;
                let truncated =
                    projection.edges.length > 0 && projection.edges.length < INITIAL_MAX_EDGES;
                const seedNodeIds = projection.nodes.map((node) => node.id);
                while (active && truncated && projection.edges.length < INITIAL_MAX_EDGES) {
                    const next = await service.listOneHopEdges(seedNodeIds, afterEdgeId);
                    projection = appendGraph(next, INITIAL_MAX_EDGES);
                    afterEdgeId = next.nextCursor;
                    truncated = next.truncated && Boolean(afterEdgeId);
                }
                if (active) setGraphState("ready");
            } catch {
                if (active) setGraphState("error");
            }
        })();
        return () => {
            active = false;
        };
    }, []);

    const expandNode = async (nodeId: string) => {
        if (expandingNodeIdsRef.current.has(nodeId)) return;
        expandingNodeIdsRef.current.add(nodeId);
        try {
            let afterEdgeId: string | null = null;
            let truncated = true;
            while (truncated && graphRef.current.edges.length < EXPANDED_MAX_EDGES) {
                const next = await service.listOneHopEdges([nodeId], afterEdgeId);
                appendGraph(next, EXPANDED_MAX_EDGES);
                afterEdgeId = next.nextCursor;
                truncated = next.truncated && Boolean(afterEdgeId);
            }
        } catch {
            // 双击展开是增强操作；保留当前已加载图谱，不将其降级为整体错误状态。
        } finally {
            expandingNodeIdsRef.current.delete(nodeId);
        }
    };

    return { expandNode, graph, graphState, overview };
};
