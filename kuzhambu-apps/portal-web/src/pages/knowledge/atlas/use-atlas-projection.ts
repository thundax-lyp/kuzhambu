import { useEffect, useState } from "react";
import * as service from "./atlas-workbench-service";
import type {
    AtlasEdgeRecord,
    AtlasGraphRecord,
    AtlasNodeRecord,
    AtlasOverviewRecord
} from "./atlas-workbench-types";

const EMPTY_GRAPH: AtlasGraphRecord = { edges: [], nodes: [] };
const MAX_EDGES = 40;

const mergeGraph = (current: AtlasGraphRecord, incoming: AtlasGraphRecord): AtlasGraphRecord => {
    const nodes = new Map<string, AtlasNodeRecord>();
    [...current.nodes, ...incoming.nodes].forEach((node) => nodes.set(node.id, node));
    const edges = new Map<string, AtlasEdgeRecord>();
    [...current.edges, ...incoming.edges].forEach((edge) => {
        if (
            edges.size < MAX_EDGES &&
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

    useEffect(() => {
        let active = true;
        void service.getOverview().then(
            (value) => active && setOverview(value),
            () => active && setOverview(null)
        );
        void (async () => {
            try {
                let projection = mergeGraph(EMPTY_GRAPH, await service.listRecentEdges());
                if (!active) return;
                setGraph(projection);
                let afterEdgeId: string | null = null;
                let truncated = projection.edges.length > 0 && projection.edges.length < MAX_EDGES;
                const seedNodeIds = projection.nodes.map((node) => node.id);
                while (active && truncated && projection.edges.length < MAX_EDGES) {
                    const next = await service.listOneHopEdges(seedNodeIds, afterEdgeId);
                    projection = mergeGraph(projection, next);
                    setGraph(projection);
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

    return { graph, graphState, overview };
};
