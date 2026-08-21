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
const AUTO_EXPAND_MAX_NODES = 100;
const AUTO_EXPAND_DELAY_MS = 1_200;
const AUTO_EXPAND_EDGE_STEP = 6;

const findAutoExpansionEdges = (
    graph: AtlasGraphRecord,
    source: AtlasGraphRecord
): { edges: AtlasEdgeRecord[]; nodeId: string } | null => {
    const visibleEdgeIds = new Set(graph.edges.map((edge) => edge.id));
    const candidatesByNodeId = new Map<string, AtlasEdgeRecord[]>();
    const degreeByNodeId = new Map<string, number>();
    graph.nodes.forEach((node) => candidatesByNodeId.set(node.id, []));
    source.edges.forEach((edge) => {
        [edge.sourceNodeId, edge.targetNodeId].forEach((nodeId) => {
            if (nodeId) degreeByNodeId.set(nodeId, (degreeByNodeId.get(nodeId) ?? 0) + 1);
        });
        if (visibleEdgeIds.has(edge.id)) return;
        [edge.sourceNodeId, edge.targetNodeId].forEach((nodeId) => {
            if (!nodeId) return;
            const candidates = candidatesByNodeId.get(nodeId);
            if (candidates && candidates.length < AUTO_EXPAND_EDGE_STEP) candidates.push(edge);
        });
    });
    const candidate = graph.nodes
        .map((node, index) => ({
            degree: degreeByNodeId.get(node.id) ?? 0,
            edges: candidatesByNodeId.get(node.id) ?? [],
            index,
            nodeId: node.id
        }))
        .filter((item) => item.edges.length > 0)
        .sort((left, right) => right.degree - left.degree || left.index - right.index)[0];
    return candidate ? { edges: candidate.edges, nodeId: candidate.nodeId } : null;
};

const limitAutoExpansionEdges = (
    graph: AtlasGraphRecord,
    edges: AtlasEdgeRecord[]
): AtlasEdgeRecord[] => {
    const acceptedNodeIds = new Set(graph.nodes.map((node) => node.id));
    const acceptedEdges: AtlasEdgeRecord[] = [];
    for (const edge of edges) {
        const endpointIds = [edge.sourceNodeId, edge.targetNodeId].filter(
            (nodeId): nodeId is string => Boolean(nodeId)
        );
        const newNodeIds = endpointIds.filter((nodeId) => !acceptedNodeIds.has(nodeId));
        if (acceptedNodeIds.size + newNodeIds.length > AUTO_EXPAND_MAX_NODES) continue;
        acceptedEdges.push(edge);
        newNodeIds.forEach((nodeId) => acceptedNodeIds.add(nodeId));
    }
    return acceptedEdges;
};

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
    const [expandedNodeIds, setExpandedNodeIds] = useState<ReadonlySet<string>>(() => new Set());
    const graphRef = useRef(graph);
    const autoExpansionSourceRef = useRef<AtlasGraphRecord>(EMPTY_GRAPH);
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
                const recentGraph = await service.listRecentEdges();
                autoExpansionSourceRef.current = recentGraph;
                let projection = mergeGraph(EMPTY_GRAPH, recentGraph, INITIAL_MAX_EDGES);
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

    useEffect(() => {
        if (graphState !== "ready") return;
        let active = true;
        let timer: ReturnType<typeof setTimeout> | undefined;
        const scheduleNextExpansion = () => {
            timer = setTimeout(() => {
                if (!active || graphRef.current.nodes.length >= AUTO_EXPAND_MAX_NODES) return;
                const candidate = findAutoExpansionEdges(
                    graphRef.current,
                    autoExpansionSourceRef.current
                );
                if (!candidate) return;
                const edges = limitAutoExpansionEdges(graphRef.current, candidate.edges);
                if (edges.length === 0) return;
                appendGraph(
                    { edges, nodes: autoExpansionSourceRef.current.nodes },
                    EXPANDED_MAX_EDGES
                );
                setExpandedNodeIds((current) => new Set(current).add(candidate.nodeId));
                scheduleNextExpansion();
            }, AUTO_EXPAND_DELAY_MS);
        };
        scheduleNextExpansion();
        return () => {
            active = false;
            if (timer) clearTimeout(timer);
        };
    }, [graphState]);

    const expandNode = async (nodeId: string) => {
        if (expandingNodeIdsRef.current.has(nodeId)) return;
        expandingNodeIdsRef.current.add(nodeId);
        const previousEdgeCount = graphRef.current.edges.length;
        try {
            let afterEdgeId: string | null = null;
            let truncated = true;
            while (truncated && graphRef.current.edges.length < EXPANDED_MAX_EDGES) {
                const next = await service.listOneHopEdges([nodeId], afterEdgeId);
                appendGraph(next, EXPANDED_MAX_EDGES);
                afterEdgeId = next.nextCursor;
                truncated = next.truncated && Boolean(afterEdgeId);
            }
            if (graphRef.current.edges.length > previousEdgeCount) {
                setExpandedNodeIds((current) => new Set(current).add(nodeId));
            }
        } catch {
            // 双击展开是增强操作；保留当前已加载图谱，不将其降级为整体错误状态。
        } finally {
            expandingNodeIdsRef.current.delete(nodeId);
        }
    };

    return {
        autoExpansionComplete: graph.nodes.length >= AUTO_EXPAND_MAX_NODES,
        expandedNodeIds,
        expandNode,
        graph,
        graphState,
        overview
    };
};
