import type { EdgeData, GraphData, NodeData } from "@antv/g6";

import type { KuzhambuGraphNodeItem, KuzhambuGraphSpoItem } from "./kuzhambu-graph-types";

const DEFAULT_GROUP = "default";
const VIRTUAL_GROUP_PREFIX = "__kuzhambu_graph_group__";

const getSubjectGroup = (spoItem: KuzhambuGraphSpoItem) => {
    return spoItem.subjectGroup || spoItem.group || DEFAULT_GROUP;
};

const getObjectGroup = (spoItem: KuzhambuGraphSpoItem) => {
    return spoItem.objectGroup || spoItem.group || DEFAULT_GROUP;
};

const createEdgeId = (spoItem: KuzhambuGraphSpoItem) => {
    return `${spoItem.subjectId ?? spoItem.subject}::${spoItem.predicate}::${spoItem.objectId ?? spoItem.object}`;
};

const createGroupAnchorId = (index: number) => `${VIRTUAL_GROUP_PREFIX}${index}`;

const isVirtualNode = (node: NodeData) => Boolean(node.data?.isVirtual);

const isVirtualEdge = (edge: EdgeData) => Boolean(edge.data?.isVirtual);

const collectComponents = (nodes: NodeData[], edges: EdgeData[]) => {
    const adjacency = new Map<string, Set<string>>();
    nodes.forEach((node) => adjacency.set(String(node.id), new Set()));
    edges.forEach((edge) => {
        const source = String(edge.source);
        const target = String(edge.target);
        adjacency.get(source)?.add(target);
        adjacency.get(target)?.add(source);
    });

    const visited = new Set<string>();
    const components: string[][] = [];
    adjacency.forEach((_, nodeId) => {
        if (visited.has(nodeId)) {
            return;
        }

        const component: string[] = [];
        const queue = [nodeId];
        visited.add(nodeId);
        while (queue.length > 0) {
            const current = queue.shift();
            if (!current) {
                continue;
            }
            component.push(current);
            adjacency.get(current)?.forEach((next) => {
                if (!visited.has(next)) {
                    visited.add(next);
                    queue.push(next);
                }
            });
        }
        components.push(component);
    });

    return components;
};

const addLayoutAnchors = (nodes: NodeData[], edges: EdgeData[]): GraphData => {
    const components = collectComponents(nodes, edges);
    if (components.length <= 1) {
        return { nodes, edges };
    }

    const anchoredNodes = [...nodes];
    const anchoredEdges = [...edges];
    components.forEach((component, index) => {
        const anchorId = createGroupAnchorId(index);
        const layoutGroup = `component-${index}`;
        anchoredNodes.push({
            id: anchorId,
            data: {
                group: layoutGroup,
                isVirtual: true,
                layoutGroup
            }
        });
        component.forEach((nodeId) => {
            const node = anchoredNodes.find((item) => item.id === nodeId);
            if (node) {
                node.data = {
                    ...node.data,
                    layoutGroup
                };
            }
            anchoredEdges.push({
                id: `${anchorId}::member::${nodeId}`,
                source: anchorId,
                target: nodeId,
                data: {
                    isVirtual: true,
                    virtualType: "group-member"
                }
            });
        });
    });

    for (let index = 1; index < components.length; index += 1) {
        anchoredEdges.push({
            id: `${createGroupAnchorId(index - 1)}::group-link::${createGroupAnchorId(index)}`,
            source: createGroupAnchorId(index - 1),
            target: createGroupAnchorId(index),
            data: {
                isVirtual: true,
                virtualType: "group-link"
            }
        });
    }

    return {
        nodes: anchoredNodes,
        edges: anchoredEdges
    };
};

export const buildKuzhambuGraphData = (
    spoList: KuzhambuGraphSpoItem[],
    nodeList: KuzhambuGraphNodeItem[] = []
): GraphData => {
    const nodeMap = new Map<string, NodeData>();
    const edgeMap = new Map<string, EdgeData>();

    nodeList.forEach((node) => {
        nodeMap.set(node.id, {
            id: node.id,
            data: {
                label: node.label,
                group: node.group ?? DEFAULT_GROUP
            }
        });
    });

    spoList.forEach((spoItem) => {
        const subjectId = spoItem.subjectId ?? spoItem.subject;
        const objectId = spoItem.objectId ?? spoItem.object;
        if (!nodeMap.has(subjectId)) {
            nodeMap.set(subjectId, {
                id: subjectId,
                data: {
                    label: spoItem.subject,
                    group: getSubjectGroup(spoItem)
                }
            });
        }

        if (!nodeMap.has(objectId)) {
            nodeMap.set(objectId, {
                id: objectId,
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
                source: subjectId,
                target: objectId,
                data: {
                    label: spoItem.predicate
                }
            });
        }
    });

    return addLayoutAnchors(Array.from(nodeMap.values()), Array.from(edgeMap.values()));
};

export const mergeKuzhambuGraphData = (
    currentData: GraphData,
    appendedData: GraphData
): GraphData => {
    const nodeMap = new Map<string, NodeData>();
    const edgeMap = new Map<string, EdgeData>();

    currentData.nodes
        ?.filter((node) => !isVirtualNode(node))
        .forEach((node) => nodeMap.set(String(node.id), node));
    appendedData.nodes?.forEach((node) => {
        if (!isVirtualNode(node) && !nodeMap.has(String(node.id))) {
            nodeMap.set(String(node.id), node);
        }
    });

    currentData.edges
        ?.filter((edge) => !isVirtualEdge(edge))
        .forEach((edge) => {
            if (edge.id) {
                edgeMap.set(String(edge.id), edge);
            }
        });
    appendedData.edges?.forEach((edge) => {
        if (!isVirtualEdge(edge) && edge.id && !edgeMap.has(String(edge.id))) {
            edgeMap.set(String(edge.id), edge);
        }
    });

    return addLayoutAnchors(Array.from(nodeMap.values()), Array.from(edgeMap.values()));
};
