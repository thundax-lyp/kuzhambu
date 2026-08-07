import type { EdgeData, GraphData, NodeData } from "@antv/g6";

import type { KuzhambuGraphSpoItem } from "./kuzhambu-graph-types";

const DEFAULT_GROUP = "default";

const getSubjectGroup = (spoItem: KuzhambuGraphSpoItem) => {
    return spoItem.subjectGroup || spoItem.group || DEFAULT_GROUP;
};

const getObjectGroup = (spoItem: KuzhambuGraphSpoItem) => {
    return spoItem.objectGroup || spoItem.group || DEFAULT_GROUP;
};

const createEdgeId = (spoItem: KuzhambuGraphSpoItem) => {
    return `${spoItem.subject}::${spoItem.predicate}::${spoItem.object}`;
};

export const buildKuzhambuGraphData = (spoList: KuzhambuGraphSpoItem[]): GraphData => {
    const nodeMap = new Map<string, NodeData>();
    const edgeMap = new Map<string, EdgeData>();

    spoList.forEach((spoItem) => {
        if (!nodeMap.has(spoItem.subject)) {
            nodeMap.set(spoItem.subject, {
                id: spoItem.subject,
                data: {
                    label: spoItem.subject,
                    group: getSubjectGroup(spoItem)
                }
            });
        }

        if (!nodeMap.has(spoItem.object)) {
            nodeMap.set(spoItem.object, {
                id: spoItem.object,
                data: {
                    label: spoItem.object,
                    group: getObjectGroup(spoItem)
                }
            });
        }

        const edgeId = createEdgeId(spoItem);
        if (!edgeMap.has(edgeId)) {
            edgeMap.set(edgeId, {
                id: edgeId,
                source: spoItem.subject,
                target: spoItem.object,
                data: {
                    label: spoItem.predicate
                }
            });
        }
    });

    return {
        nodes: Array.from(nodeMap.values()),
        edges: Array.from(edgeMap.values())
    };
};

export const mergeKuzhambuGraphData = (
    currentData: GraphData,
    appendedData: GraphData
): GraphData => {
    const nodeMap = new Map<string, NodeData>();
    const edgeMap = new Map<string, EdgeData>();

    currentData.nodes?.forEach((node) => nodeMap.set(String(node.id), node));
    appendedData.nodes?.forEach((node) => {
        if (!nodeMap.has(String(node.id))) {
            nodeMap.set(String(node.id), node);
        }
    });

    currentData.edges?.forEach((edge) => {
        if (edge.id) {
            edgeMap.set(String(edge.id), edge);
        }
    });
    appendedData.edges?.forEach((edge) => {
        if (edge.id && !edgeMap.has(String(edge.id))) {
            edgeMap.set(String(edge.id), edge);
        }
    });

    return {
        nodes: Array.from(nodeMap.values()),
        edges: Array.from(edgeMap.values())
    };
};
