import type { AtlasEdgeRecord, AtlasGraphRecord, AtlasNodeRecord } from "./atlas-workbench-types";

export interface AtlasLayoutNode {
    entryOffset?: Position;
    id: string;
    node: AtlasNodeRecord;
    position: { x: number; y: number };
}

export interface AtlasLayoutEdge extends AtlasEdgeRecord {
    relationLabel: string;
}

export interface AtlasForceLayoutResult {
    edges: AtlasLayoutEdge[];
    nodes: AtlasLayoutNode[];
}

interface Position {
    x: number;
    y: number;
}

interface Particle extends Position {
    id: string;
    hidden: boolean;
    vx: number;
    vy: number;
}

interface Cluster {
    anchorIds: [string, string];
    ids: string[];
    index: number;
    weight: number;
}

const MAX_VISIBLE_CLUSTERS = 6;
const INCREMENTAL_RELAXATION_ITERATIONS = 48;
const FINAL_RELAXATION_ITERATIONS = 180;
// 名称是视觉标注，不参与 React Flow 节点尺寸、碰撞或边的端点计算。
export const ATLAS_NODE_SIZE = { height: 28, width: 28 };
const DEFAULT_POSITION = { x: 480, y: 340 };

const relationLabels: Record<string, string> = {
    ANCESTOR_OF: "祖先/后裔",
    ASSOCIATED_WITH: "关联",
    AUTHORED: "著作",
    CAUSES: "导致",
    COMPILED: "编纂",
    DEPICTS: "描绘",
    DESCRIBES: "记述",
    HOLDS_OFFICE: "任职",
    LOCATED_IN: "位于",
    MADE_OF: "制成",
    MEMBER_OF: "隶属",
    OCCURS_AT: "发生于",
    PARENT_OF: "父母/子女",
    PARTICIPATED_IN: "参与",
    PART_OF: "组成",
    PRACTICES: "实践",
    RULES: "统治",
    SPOUSE_OF: "配偶",
    SUCCEEDS: "继任",
    USES: "使用",
    WORSHIPS: "崇祀"
};

const relationLabel = (relationType?: string | null) =>
    (relationType ? relationLabels[relationType] : undefined) || "关联";

const compareCluster = (
    left: string[],
    right: string[],
    degrees: Map<string, number>,
    nodesById: Map<string, AtlasNodeRecord>
) => {
    const representativeName = (cluster: string[]) =>
        [...cluster]
            .sort((first, second) => {
                const degreeDifference = (degrees.get(second) ?? 0) - (degrees.get(first) ?? 0);
                if (degreeDifference !== 0) return degreeDifference;
                return (nodesById.get(first)?.name || "").localeCompare(
                    nodesById.get(second)?.name || "",
                    "zh-CN"
                );
            })
            .map((id) => nodesById.get(id)?.name || "")
            .join("\u0000");
    return (
        right.length - left.length ||
        representativeName(left).localeCompare(representativeName(right), "zh-CN")
    );
};

/**
 * 从低度数候选中挑选距离最远的一对锚点。
 *
 * 低度数节点通常位于簇的外缘；再用簇内最短路径的最大值挑选一对，能让两条隐藏连接
 * 从簇的不同侧引出，避免把整个簇朝同一个方向拉成长条。
 */
const selectAnchors = (clusterIds: string[], adjacency: Map<string, Set<string>>) => {
    const candidates = [...clusterIds]
        .sort((left, right) => (adjacency.get(left)?.size ?? 0) - (adjacency.get(right)?.size ?? 0))
        .slice(0, Math.min(4, clusterIds.length));
    if (candidates.length === 1) return [candidates[0], candidates[0]] as [string, string];

    let anchors: [string, string] = [candidates[0], candidates[1]];
    let maximumDistance = -1;
    candidates.forEach((source, sourceIndex) => {
        const distances = new Map([[source, 0]]);
        const queue = [source];
        for (let cursor = 0; cursor < queue.length; cursor += 1) {
            const current = queue[cursor];
            adjacency.get(current)?.forEach((neighbor) => {
                if (!distances.has(neighbor)) {
                    distances.set(neighbor, (distances.get(current) ?? 0) + 1);
                    queue.push(neighbor);
                }
            });
        }
        candidates.slice(sourceIndex + 1).forEach((target) => {
            const distance = distances.get(target) ?? 0;
            if (distance > maximumDistance) {
                maximumDistance = distance;
                anchors = [source, target];
            }
        });
    });
    return anchors;
};

const addForce = (forces: Map<string, Position>, id: string, x: number, y: number) => {
    const force = forces.get(id) || { x: 0, y: 0 };
    force.x += x;
    force.y += y;
    forces.set(id, force);
};

const applySpring = (
    particles: Map<string, Particle>,
    forces: Map<string, Position>,
    sourceId: string,
    targetId: string,
    targetDistance: number,
    strength: number
) => {
    const source = particles.get(sourceId);
    const target = particles.get(targetId);
    if (!source || !target) return;
    const dx = target.x - source.x;
    const dy = target.y - source.y;
    const distance = Math.max(1, Math.hypot(dx, dy));
    const magnitude = (distance - targetDistance) * strength;
    const fx = (dx / distance) * magnitude;
    const fy = (dy / distance) * magnitude;
    addForce(forces, sourceId, fx, fy);
    addForce(forces, targetId, -fx, -fy);
};

/**
 * 为 Portal 总谱预览计算确定性的力导向坐标。
 *
 * 1. 先用真实关系找连通分量；孤立节点不进入预览。
 * 2. 以簇大小为权重，选出最多六个最大的簇；同权重以簇内最高度数节点的名字稳定排序。
 * 3. 每簇选择两个低度数、但彼此路径最远的锚点。相邻簇之间插入仅用于计算的隐藏节点，
 *    将锚点串成环。隐藏边强度和簇权重成反比，因此大簇会拥有更多展开空间。
 * 4. 真实节点之间施加强斥力、真实边施加强引力；隐藏节点和隐藏边永远不返回给渲染层。
 */
export const layoutAtlasForceGraph = (graph: AtlasGraphRecord): AtlasForceLayoutResult => {
    const nodesById = new Map(graph.nodes.filter((node) => node.id).map((node) => [node.id, node]));
    const adjacency = new Map([...nodesById.keys()].map((id) => [id, new Set<string>()]));
    const validEdges = graph.edges.filter(
        (edge) =>
            Boolean(edge.sourceNodeId && edge.targetNodeId) &&
            edge.sourceNodeId !== edge.targetNodeId &&
            nodesById.has(edge.sourceNodeId || "") &&
            nodesById.has(edge.targetNodeId || "")
    );
    validEdges.forEach((edge) => {
        adjacency.get(edge.sourceNodeId || "")?.add(edge.targetNodeId || "");
        adjacency.get(edge.targetNodeId || "")?.add(edge.sourceNodeId || "");
    });
    const degrees = new Map([...adjacency].map(([id, neighbors]) => [id, neighbors.size]));

    const unvisited = new Set(nodesById.keys());
    const components: string[][] = [];
    while (unvisited.size > 0) {
        const start = unvisited.values().next().value as string;
        const component = [start];
        unvisited.delete(start);
        for (let cursor = 0; cursor < component.length; cursor += 1) {
            adjacency.get(component[cursor])?.forEach((neighbor) => {
                if (unvisited.delete(neighbor)) component.push(neighbor);
            });
        }
        if (component.length > 1) components.push(component);
    }

    const clusters: Cluster[] = components
        .sort((left, right) => compareCluster(left, right, degrees, nodesById))
        .slice(0, MAX_VISIBLE_CLUSTERS)
        .map((ids, index) => ({
            anchorIds: selectAnchors(ids, adjacency),
            ids,
            index,
            weight: ids.length
        }));
    const visibleIds = new Set(clusters.flatMap((cluster) => cluster.ids));
    const visibleEdges = validEdges
        .filter(
            (edge) =>
                visibleIds.has(edge.sourceNodeId || "") && visibleIds.has(edge.targetNodeId || "")
        )
        .map((edge) => ({ ...edge, relationLabel: relationLabel(edge.relationType) }));
    if (clusters.length === 0) return { edges: [], nodes: [] };

    // 这是预览画布，不是无限画布：环半径保留簇间空隙，同时避免 fitView 把真实节点缩得过小。
    const ringRadius = Math.max(250, 90 + clusters.length * 70);
    const particles = new Map<string, Particle>();
    const centers = new Map<number, Position>();
    clusters.forEach((cluster) => {
        const angle = (Math.PI * 2 * cluster.index) / clusters.length - Math.PI / 2;
        const center = {
            x: DEFAULT_POSITION.x + Math.cos(angle) * ringRadius,
            y: DEFAULT_POSITION.y + Math.sin(angle) * ringRadius * 0.7
        };
        centers.set(cluster.index, center);
        cluster.ids.forEach((id, index) => {
            const nodeAngle = (Math.PI * 2 * index) / cluster.ids.length + angle;
            const localRadius = 52 + Math.min(70, Math.sqrt(cluster.weight) * 16);
            particles.set(id, {
                hidden: false,
                id,
                vx: 0,
                vy: 0,
                x: center.x + Math.cos(nodeAngle) * localRadius,
                y: center.y + Math.sin(nodeAngle) * localRadius
            });
        });
    });

    // 隐藏节点位于相邻簇之间；它们只在此 Map 内存在，后续不会成为 React Flow 元素。
    const hiddenIds = clusters.length > 1 ? clusters.map((_, index) => `hidden-gap-${index}`) : [];
    hiddenIds.forEach((id, index) => {
        const currentCenter = centers.get(index) || DEFAULT_POSITION;
        const nextCenter = centers.get((index + 1) % clusters.length) || DEFAULT_POSITION;
        particles.set(id, {
            hidden: true,
            id,
            vx: 0,
            vy: 0,
            x: (currentCenter.x + nextCenter.x) / 2,
            y: (currentCenter.y + nextCenter.y) / 2
        });
    });

    for (let iteration = 0; iteration < 220; iteration += 1) {
        const forces = new Map<string, Position>();
        clusters.forEach((cluster) => {
            const center = centers.get(cluster.index) || DEFAULT_POSITION;
            cluster.ids.forEach((id, index) => {
                const particle = particles.get(id)!;
                // 轻微回心力仅防止簇漂移；节点间斥力和真实边决定簇的实际形状。
                addForce(
                    forces,
                    id,
                    (center.x - particle.x) * 0.009,
                    (center.y - particle.y) * 0.009
                );
                cluster.ids.slice(index + 1).forEach((otherId) => {
                    const other = particles.get(otherId)!;
                    const dx = other.x - particle.x;
                    const dy = other.y - particle.y;
                    const distance = Math.max(1, Math.hypot(dx, dy));
                    const magnitude = Math.min(3.4, 15500 / (distance * distance));
                    addForce(
                        forces,
                        id,
                        (-dx / distance) * magnitude,
                        (-dy / distance) * magnitude
                    );
                    addForce(
                        forces,
                        otherId,
                        (dx / distance) * magnitude,
                        (dy / distance) * magnitude
                    );
                });
            });
        });

        visibleEdges.forEach((edge) =>
            applySpring(
                particles,
                forces,
                edge.sourceNodeId || "",
                edge.targetNodeId || "",
                148,
                0.022
            )
        );
        hiddenIds.forEach((hiddenId, index) => {
            const currentCluster = clusters[index];
            const nextCluster = clusters[(index + 1) % clusters.length];
            const currentStrength = 0.022 / Math.sqrt(currentCluster.weight);
            const nextStrength = 0.022 / Math.sqrt(nextCluster.weight);
            applySpring(
                particles,
                forces,
                currentCluster.anchorIds[1],
                hiddenId,
                180,
                currentStrength
            );
            applySpring(particles, forces, nextCluster.anchorIds[0], hiddenId, 180, nextStrength);
            applySpring(
                particles,
                forces,
                hiddenId,
                hiddenIds[(index + 1) % hiddenIds.length],
                Math.max(220, ringRadius * 0.86),
                0.003
            );
        });

        particles.forEach((particle) => {
            const force = forces.get(particle.id) || { x: 0, y: 0 };
            particle.vx = (particle.vx + force.x) * 0.72;
            particle.vy = (particle.vy + force.y) * 0.72;
            particle.x += particle.vx;
            particle.y += particle.vy;
            if (!Number.isFinite(particle.x) || !Number.isFinite(particle.y)) {
                particle.x = DEFAULT_POSITION.x;
                particle.y = DEFAULT_POSITION.y;
                particle.vx = 0;
                particle.vy = 0;
            }
        });
    }

    return {
        edges: visibleEdges,
        nodes: graph.nodes
            .filter((node) => visibleIds.has(node.id))
            .map((node) => {
                const particle = particles.get(node.id);
                return {
                    id: node.id,
                    node,
                    position: {
                        x: (particle?.x ?? DEFAULT_POSITION.x) - ATLAS_NODE_SIZE.width / 2,
                        y: (particle?.y ?? DEFAULT_POSITION.y) - ATLAS_NODE_SIZE.height / 2
                    }
                };
            })
    };
};

const edgeIsValid = (edge: AtlasEdgeRecord, nodes: Map<string, AtlasNodeRecord>) =>
    Boolean(edge.sourceNodeId && edge.targetNodeId) &&
    edge.sourceNodeId !== edge.targetNodeId &&
    nodes.has(edge.sourceNodeId || "") &&
    nodes.has(edge.targetNodeId || "");

const positionKey = (leftRoot: string, rightRoot: string) => `${leftRoot}\u0000${rightRoot}`;

/**
 * Atlas 的增量布局状态。
 *
 * 首帧使用完整力导向布局建立稳定基线；后续图数据只增不减时，节点通过并查集持有簇归属。
 * 新边连接不同簇时只移动较小簇并合并簇记录，其他簇的位置不会重新计算。
 */
export class AtlasIncrementalLayout {
    private readonly edges = new Map<string, AtlasEdgeRecord>();
    private readonly entryOrigins = new Map<string, Position>();
    private readonly hiddenGaps = new Map<string, [string, string]>();
    private readonly nodes = new Map<string, AtlasNodeRecord>();
    private readonly parents = new Map<string, string>();
    private readonly positions = new Map<string, Position>();
    private readonly sizes = new Map<string, number>();
    private readonly visibleClusterRoots = new Set<string>();
    private clusterOrder: string[] = [];
    private initialized = false;

    update(graph: AtlasGraphRecord, finalRelaxation = false): AtlasForceLayoutResult {
        if (
            !this.initialized ||
            (this.edges.size === 0 && graph.edges.length > 0) ||
            this.hasRemoval(graph)
        )
            this.initialize(graph);
        else
            this.append(
                graph,
                finalRelaxation ? FINAL_RELAXATION_ITERATIONS : INCREMENTAL_RELAXATION_ITERATIONS
            );
        return this.project(graph);
    }

    getClusterId(nodeId: string) {
        return this.parents.has(nodeId) ? this.find(nodeId) : null;
    }

    getHiddenGapCount() {
        return this.hiddenGaps.size;
    }

    private initialize(graph: AtlasGraphRecord) {
        this.edges.clear();
        this.entryOrigins.clear();
        this.hiddenGaps.clear();
        this.nodes.clear();
        this.parents.clear();
        this.positions.clear();
        this.sizes.clear();
        this.visibleClusterRoots.clear();
        graph.nodes.forEach((node) => this.addNode(node));
        graph.edges
            .filter((edge) => edgeIsValid(edge, this.nodes))
            .forEach((edge) => {
                this.edges.set(edge.id, edge);
                this.union(edge.sourceNodeId || "", edge.targetNodeId || "");
            });
        const initial = layoutAtlasForceGraph(graph);
        initial.nodes.forEach(({ id, position }) => {
            this.positions.set(id, {
                x: position.x + ATLAS_NODE_SIZE.width / 2,
                y: position.y + ATLAS_NODE_SIZE.height / 2
            });
            this.visibleClusterRoots.add(this.find(id));
        });
        this.clusterOrder = this.orderVisibleClustersByPosition();
        this.rebuildHiddenGaps();
        this.initialized = true;
    }

    private append(graph: AtlasGraphRecord, relaxationIterations: number) {
        graph.nodes.forEach((node) => {
            if (!this.nodes.has(node.id)) this.addNode(node);
            else this.nodes.set(node.id, node);
        });
        const affectedNodeIds = new Set<string>();
        graph.edges
            .filter((edge) => !this.edges.has(edge.id) && edgeIsValid(edge, this.nodes))
            .forEach((edge) => {
                this.attachEdge(edge);
                this.edges.set(edge.id, edge);
                if (edge.sourceNodeId) affectedNodeIds.add(edge.sourceNodeId);
                if (edge.targetNodeId) affectedNodeIds.add(edge.targetNodeId);
            });
        new Set([...affectedNodeIds].map((nodeId) => this.find(nodeId))).forEach((root) =>
            this.relaxCluster(root, relaxationIterations)
        );
    }

    private attachEdge(edge: AtlasEdgeRecord) {
        const sourceId = edge.sourceNodeId || "";
        const targetId = edge.targetNodeId || "";
        const sourceRoot = this.find(sourceId);
        const targetRoot = this.find(targetId);
        this.seedMissingEndpointPosition(sourceId, targetId, edge.id);
        if (sourceRoot === targetRoot) return;

        const sourceVisible = this.visibleClusterRoots.has(sourceRoot);
        const targetVisible = this.visibleClusterRoots.has(targetRoot);
        const movingRoot =
            (this.sizes.get(sourceRoot) ?? 1) <= (this.sizes.get(targetRoot) ?? 1)
                ? sourceRoot
                : targetRoot;
        const fixedRoot = movingRoot === sourceRoot ? targetRoot : sourceRoot;
        const movingEndpointId = movingRoot === sourceRoot ? sourceId : targetId;
        const fixedEndpointId = movingRoot === sourceRoot ? targetId : sourceId;
        this.moveClusterBeside(movingRoot, movingEndpointId, fixedEndpointId, edge.id);

        const mergedRoot = this.union(sourceRoot, targetRoot);
        this.visibleClusterRoots.delete(sourceRoot);
        this.visibleClusterRoots.delete(targetRoot);
        if (sourceVisible || targetVisible) this.visibleClusterRoots.add(mergedRoot);
        this.clusterOrder = this.clusterOrder
            .map((root) =>
                root === sourceRoot || root === targetRoot ? mergedRoot : this.find(root)
            )
            .filter((root, index, roots) => roots.indexOf(root) === index);
        if ((sourceVisible || targetVisible) && !this.clusterOrder.includes(mergedRoot)) {
            const fixedIndex = this.clusterOrder.indexOf(fixedRoot);
            this.clusterOrder.splice(
                fixedIndex < 0 ? this.clusterOrder.length : fixedIndex + 1,
                0,
                mergedRoot
            );
        }
        this.rebuildHiddenGaps();
    }

    private seedMissingEndpointPosition(sourceId: string, targetId: string, edgeId: string) {
        const source = this.positions.get(sourceId);
        const target = this.positions.get(targetId);
        if (source && !target) {
            this.entryOrigins.set(targetId, source);
            this.positions.set(targetId, this.positionNear(source, edgeId));
        } else if (!source && target) {
            this.entryOrigins.set(sourceId, target);
            this.positions.set(sourceId, this.positionNear(target, edgeId));
        } else if (!source && !target) {
            const sourcePosition = this.positionNear(DEFAULT_POSITION, edgeId);
            this.entryOrigins.set(sourceId, DEFAULT_POSITION);
            this.entryOrigins.set(targetId, sourcePosition);
            this.positions.set(sourceId, sourcePosition);
            this.positions.set(targetId, this.positionNear(sourcePosition, `${edgeId}-target`));
        }
    }

    private moveClusterBeside(
        movingRoot: string,
        movingEndpointId: string,
        fixedEndpointId: string,
        edgeId: string
    ) {
        const movingEndpoint = this.positions.get(movingEndpointId) || DEFAULT_POSITION;
        const fixedEndpoint = this.positions.get(fixedEndpointId) || DEFAULT_POSITION;
        let dx = movingEndpoint.x - fixedEndpoint.x;
        let dy = movingEndpoint.y - fixedEndpoint.y;
        if (Math.hypot(dx, dy) < 1) {
            const seeded = this.positionNear(fixedEndpoint, edgeId);
            dx = seeded.x - fixedEndpoint.x;
            dy = seeded.y - fixedEndpoint.y;
        }
        const distance = Math.max(1, Math.hypot(dx, dy));
        const desired = {
            x: fixedEndpoint.x + (dx / distance) * 148,
            y: fixedEndpoint.y + (dy / distance) * 148
        };
        const translation = {
            x: desired.x - movingEndpoint.x,
            y: desired.y - movingEndpoint.y
        };
        this.nodes.forEach((_, nodeId) => {
            if (this.find(nodeId) !== movingRoot) return;
            const position = this.positions.get(nodeId) || movingEndpoint;
            this.positions.set(nodeId, {
                x: position.x + translation.x,
                y: position.y + translation.y
            });
        });
    }

    private relaxCluster(root: string, iterations: number) {
        const ids = [...this.nodes.keys()].filter(
            (nodeId) => this.find(nodeId) === root && this.positions.has(nodeId)
        );
        if (ids.length < 2) return;
        const centerBefore = ids.reduce(
            (center, nodeId) => {
                const position = this.positions.get(nodeId) || DEFAULT_POSITION;
                center.x += position.x / ids.length;
                center.y += position.y / ids.length;
                return center;
            },
            { x: 0, y: 0 }
        );
        const particles = new Map(
            ids.map((id) => {
                const position = this.positions.get(id) || centerBefore;
                return [id, { ...position, hidden: false, id, vx: 0, vy: 0 } satisfies Particle];
            })
        );
        const clusterEdges = [...this.edges.values()].filter(
            (edge) =>
                particles.has(edge.sourceNodeId || "") && particles.has(edge.targetNodeId || "")
        );
        for (let iteration = 0; iteration < iterations; iteration += 1) {
            const forces = new Map<string, Position>();
            ids.forEach((id, index) => {
                const particle = particles.get(id)!;
                addForce(
                    forces,
                    id,
                    (centerBefore.x - particle.x) * 0.004,
                    (centerBefore.y - particle.y) * 0.004
                );
                for (let otherIndex = index + 1; otherIndex < ids.length; otherIndex += 1) {
                    const otherId = ids[otherIndex];
                    const other = particles.get(otherId)!;
                    const dx = other.x - particle.x;
                    const dy = other.y - particle.y;
                    const distance = Math.max(1, Math.hypot(dx, dy));
                    const magnitude = Math.min(2.8, 12_000 / (distance * distance));
                    addForce(
                        forces,
                        id,
                        (-dx / distance) * magnitude,
                        (-dy / distance) * magnitude
                    );
                    addForce(
                        forces,
                        otherId,
                        (dx / distance) * magnitude,
                        (dy / distance) * magnitude
                    );
                }
            });
            clusterEdges.forEach((edge) =>
                applySpring(
                    particles,
                    forces,
                    edge.sourceNodeId || "",
                    edge.targetNodeId || "",
                    148,
                    0.022
                )
            );
            particles.forEach((particle) => {
                const force = forces.get(particle.id) || { x: 0, y: 0 };
                particle.vx = (particle.vx + force.x) * 0.7;
                particle.vy = (particle.vy + force.y) * 0.7;
                particle.x += particle.vx;
                particle.y += particle.vy;
            });
        }
        const centerAfter = [...particles.values()].reduce(
            (center, particle) => {
                center.x += particle.x / particles.size;
                center.y += particle.y / particles.size;
                return center;
            },
            { x: 0, y: 0 }
        );
        particles.forEach((particle) => {
            this.positions.set(particle.id, {
                x: particle.x + centerBefore.x - centerAfter.x,
                y: particle.y + centerBefore.y - centerAfter.y
            });
        });
    }

    private positionNear(position: Position, seed: string): Position {
        let hash = 0;
        for (let index = 0; index < seed.length; index += 1) {
            hash = (hash * 31 + seed.charCodeAt(index)) | 0;
        }
        const angle = ((Math.abs(hash) % 360) * Math.PI) / 180;
        return {
            x: position.x + Math.cos(angle) * 148,
            y: position.y + Math.sin(angle) * 148
        };
    }

    private addNode(node: AtlasNodeRecord) {
        this.nodes.set(node.id, node);
        this.parents.set(node.id, node.id);
        this.sizes.set(node.id, 1);
    }

    private find(nodeId: string): string {
        const parent = this.parents.get(nodeId) || nodeId;
        if (parent === nodeId) return nodeId;
        const root = this.find(parent);
        this.parents.set(nodeId, root);
        return root;
    }

    private union(leftId: string, rightId: string): string {
        let leftRoot = this.find(leftId);
        let rightRoot = this.find(rightId);
        if (leftRoot === rightRoot) return leftRoot;
        if ((this.sizes.get(leftRoot) ?? 1) < (this.sizes.get(rightRoot) ?? 1)) {
            [leftRoot, rightRoot] = [rightRoot, leftRoot];
        }
        this.parents.set(rightRoot, leftRoot);
        this.sizes.set(
            leftRoot,
            (this.sizes.get(leftRoot) ?? 1) + (this.sizes.get(rightRoot) ?? 1)
        );
        this.sizes.delete(rightRoot);
        return leftRoot;
    }

    private orderVisibleClustersByPosition() {
        const centers = new Map<string, { count: number; x: number; y: number }>();
        this.positions.forEach((position, nodeId) => {
            const root = this.find(nodeId);
            const center = centers.get(root) || { count: 0, x: 0, y: 0 };
            center.count += 1;
            center.x += position.x;
            center.y += position.y;
            centers.set(root, center);
        });
        const overall = [...centers.values()].reduce(
            (value, center) => ({
                x: value.x + center.x / center.count,
                y: value.y + center.y / center.count
            }),
            { x: 0, y: 0 }
        );
        const count = Math.max(1, centers.size);
        overall.x /= count;
        overall.y /= count;
        return [...this.visibleClusterRoots].sort((left, right) => {
            const leftCenter = centers.get(left) || { count: 1, x: 0, y: 0 };
            const rightCenter = centers.get(right) || { count: 1, x: 0, y: 0 };
            return (
                Math.atan2(
                    leftCenter.y / leftCenter.count - overall.y,
                    leftCenter.x / leftCenter.count - overall.x
                ) -
                Math.atan2(
                    rightCenter.y / rightCenter.count - overall.y,
                    rightCenter.x / rightCenter.count - overall.x
                )
            );
        });
    }

    private rebuildHiddenGaps() {
        this.hiddenGaps.clear();
        if (this.clusterOrder.length <= 1) return;
        this.clusterOrder.forEach((leftRoot, index) => {
            const rightRoot = this.clusterOrder[(index + 1) % this.clusterOrder.length];
            this.hiddenGaps.set(positionKey(leftRoot, rightRoot), [leftRoot, rightRoot]);
        });
    }

    private hasRemoval(graph: AtlasGraphRecord) {
        const nodeIds = new Set(graph.nodes.map((node) => node.id));
        const edgeIds = new Set(graph.edges.map((edge) => edge.id));
        return (
            [...this.nodes.keys()].some((id) => !nodeIds.has(id)) ||
            [...this.edges.keys()].some((id) => !edgeIds.has(id))
        );
    }

    private project(graph: AtlasGraphRecord): AtlasForceLayoutResult {
        const visibleIds = new Set(
            graph.nodes
                .filter(
                    (node) =>
                        this.visibleClusterRoots.has(this.find(node.id)) &&
                        this.positions.has(node.id)
                )
                .map((node) => node.id)
        );
        return {
            edges: graph.edges
                .filter(
                    (edge) =>
                        visibleIds.has(edge.sourceNodeId || "") &&
                        visibleIds.has(edge.targetNodeId || "")
                )
                .map((edge) => ({ ...edge, relationLabel: relationLabel(edge.relationType) })),
            nodes: graph.nodes
                .filter((node) => visibleIds.has(node.id))
                .map((node) => {
                    const position = this.positions.get(node.id) || DEFAULT_POSITION;
                    const entryOrigin = this.entryOrigins.get(node.id);
                    return {
                        entryOffset: entryOrigin
                            ? {
                                  x: entryOrigin.x - position.x,
                                  y: entryOrigin.y - position.y
                              }
                            : undefined,
                        id: node.id,
                        node,
                        position: {
                            x: position.x - ATLAS_NODE_SIZE.width / 2,
                            y: position.y - ATLAS_NODE_SIZE.height / 2
                        }
                    };
                })
        };
    }
}
