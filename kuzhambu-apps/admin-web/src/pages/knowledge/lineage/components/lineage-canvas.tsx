import { FullscreenOutlined, ZoomInOutlined, ZoomOutOutlined } from "@ant-design/icons";
import { Empty, Tooltip } from "antd";
import { useMemo, useRef, useState } from "react";
import type { MouseEvent as ReactMouseEvent, WheelEvent as ReactWheelEvent } from "react";
import type { LineageNodeRecord, LineageRelationRecord } from "../lineage-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

interface LineageCanvasProps {
    nodes: LineageNodeRecord[];
    relations: LineageRelationRecord[];
    selectedNodeId?: number | null;
    selectedRelationId?: number | null;
    onSelectNode: (node: LineageNodeRecord) => void;
    onSelectRelation: (relation: LineageRelationRecord) => void;
}

interface CanvasItem extends LineageNodeRecord {
    canvasX: number;
    canvasY: number;
}

interface CanvasViewport {
    height: number;
    width: number;
    x: number;
    y: number;
}

const CANVAS_WIDTH = 960;
const CANVAS_HEIGHT = 520;
const NODE_WIDTH = 128;
const NODE_HEIGHT = 58;
const DEFAULT_VIEW_BOX: CanvasViewport = {
    x: 0,
    y: 0,
    width: CANVAS_WIDTH,
    height: CANVAS_HEIGHT
};

const readStatusClassName = (status?: string | null) => {
    switch (status) {
        case "CONFIRMED":
            return "is-confirmed";
        case "REJECTED":
            return "is-rejected";
        default:
            return "is-pending";
    }
};

const layoutNodes = (nodes: LineageNodeRecord[]) => {
    const groupedNodes = nodes.reduce<Map<number, LineageNodeRecord[]>>((groups, node, index) => {
        const generation = node.generation ?? index;
        const currentNodes = groups.get(generation) || [];
        groups.set(generation, [...currentNodes, node]);
        return groups;
    }, new Map());
    const generations = Array.from(groupedNodes.keys()).sort((left, right) => left - right);
    const columnCount = Math.max(generations.length, 1);
    const columnGap = CANVAS_WIDTH / (columnCount + 1);

    return generations.flatMap((generation, columnIndex) => {
        const generationNodes = groupedNodes.get(generation) || [];
        const rowGap = CANVAS_HEIGHT / (generationNodes.length + 1);
        return generationNodes.map<CanvasItem>((node, rowIndex) => ({
            ...node,
            canvasX: node.x ?? columnGap * (columnIndex + 1),
            canvasY: node.y ?? rowGap * (rowIndex + 1)
        }));
    });
};

const readRelationLabel = (relation: LineageRelationRecord) => {
    return relation.relationLabel || relation.relationType || "关系";
};

const readRelationNodeIds = (relations: LineageRelationRecord[], nodeId?: number | null) => {
    if (nodeId == null) {
        return new Set<number>();
    }
    return relations.reduce<Set<number>>(
        (nodeIds, relation) => {
            if (relation.sourceNodeId === nodeId || relation.targetNodeId === nodeId) {
                if (relation.sourceNodeId != null) {
                    nodeIds.add(relation.sourceNodeId);
                }
                if (relation.targetNodeId != null) {
                    nodeIds.add(relation.targetNodeId);
                }
            }
            return nodeIds;
        },
        new Set([nodeId])
    );
};

const formatNodeName = (node: LineageNodeRecord) => {
    return node.name || node.nodeKey || `节点 ${node.nodeId}`;
};

export const LineageCanvas = ({
    nodes,
    relations,
    selectedNodeId,
    selectedRelationId,
    onSelectNode,
    onSelectRelation
}: LineageCanvasProps) => {
    const [viewBox, setViewBox] = useState<CanvasViewport>(DEFAULT_VIEW_BOX);
    const dragStart = useRef<{ clientX: number; clientY: number; viewBox: CanvasViewport } | null>(
        null
    );
    const canvasNodes = useMemo(() => layoutNodes(nodes), [nodes]);
    const nodeById = useMemo(() => {
        return canvasNodes.reduce<Map<number, CanvasItem>>((nodesById, node) => {
            nodesById.set(node.nodeId, node);
            return nodesById;
        }, new Map());
    }, [canvasNodes]);
    const selectedNeighborNodeIds = useMemo(
        () => readRelationNodeIds(relations, selectedNodeId),
        [relations, selectedNodeId]
    );

    const zoom = (ratio: number) => {
        setViewBox((current) => {
            const nextWidth = Math.min(
                CANVAS_WIDTH * 1.6,
                Math.max(CANVAS_WIDTH * 0.35, current.width * ratio)
            );
            const nextHeight = Math.min(
                CANVAS_HEIGHT * 1.6,
                Math.max(CANVAS_HEIGHT * 0.35, current.height * ratio)
            );
            return {
                x: current.x + (current.width - nextWidth) / 2,
                y: current.y + (current.height - nextHeight) / 2,
                width: nextWidth,
                height: nextHeight
            };
        });
    };

    const handleWheel = (event: ReactWheelEvent<SVGSVGElement>) => {
        event.preventDefault();
        zoom(event.deltaY > 0 ? 1.08 : 0.92);
    };

    const startPan = (event: ReactMouseEvent<SVGSVGElement>) => {
        if (event.button !== 0) {
            return;
        }
        dragStart.current = {
            clientX: event.clientX,
            clientY: event.clientY,
            viewBox
        };
    };

    const movePan = (event: ReactMouseEvent<SVGSVGElement>) => {
        if (!dragStart.current) {
            return;
        }
        const scaleX = viewBox.width / CANVAS_WIDTH;
        const scaleY = viewBox.height / CANVAS_HEIGHT;
        const deltaX = (event.clientX - dragStart.current.clientX) * scaleX;
        const deltaY = (event.clientY - dragStart.current.clientY) * scaleY;
        setViewBox({
            ...dragStart.current.viewBox,
            x: dragStart.current.viewBox.x - deltaX,
            y: dragStart.current.viewBox.y - deltaY
        });
    };

    const stopPan = () => {
        dragStart.current = null;
    };

    if (nodes.length === 0) {
        return (
            <Empty
                description="当前筛选没有可展示的世系节点"
                image={Empty.PRESENTED_IMAGE_SIMPLE}
            />
        );
    }

    return (
        <div className="knowledge-lineage-canvas">
            <div className="knowledge-lineage-canvas__tools" aria-label="世系画布工具">
                <Tooltip title="缩小">
                    <KuzhambuButton
                        testId="knowledge-lineage-lineage-canvas-action-button"
                        icon={<ZoomOutOutlined />}
                        onClick={() => zoom(1.12)}
                    />
                </Tooltip>
                <Tooltip title="放大">
                    <KuzhambuButton
                        testId="knowledge-lineage-lineage-canvas-action-button-2"
                        icon={<ZoomInOutlined />}
                        onClick={() => zoom(0.88)}
                    />
                </Tooltip>
                <Tooltip title="适配视图">
                    <KuzhambuButton
                        testId="knowledge-lineage-lineage-canvas-action-button-3"
                        icon={<FullscreenOutlined />}
                        onClick={() => setViewBox(DEFAULT_VIEW_BOX)}
                    />
                </Tooltip>
            </div>
            <svg
                aria-label="世系图画布"
                className="knowledge-lineage-canvas__svg"
                role="img"
                viewBox={`${viewBox.x} ${viewBox.y} ${viewBox.width} ${viewBox.height}`}
                onMouseDown={startPan}
                onMouseLeave={stopPan}
                onMouseMove={movePan}
                onMouseUp={stopPan}
                onWheel={handleWheel}
            >
                <g className="knowledge-lineage-canvas__relations">
                    {relations.map((relation) => {
                        const sourceNode =
                            relation.sourceNodeId == null
                                ? null
                                : nodeById.get(relation.sourceNodeId);
                        const targetNode =
                            relation.targetNodeId == null
                                ? null
                                : nodeById.get(relation.targetNodeId);
                        if (!sourceNode || !targetNode) {
                            return null;
                        }
                        const isSelected = relation.relationId === selectedRelationId;
                        const isAdjacent =
                            selectedNodeId == null ||
                            relation.sourceNodeId === selectedNodeId ||
                            relation.targetNodeId === selectedNodeId;
                        const labelX = (sourceNode.canvasX + targetNode.canvasX) / 2;
                        const labelY = (sourceNode.canvasY + targetNode.canvasY) / 2 - 8;
                        return (
                            <g
                                className={[
                                    "knowledge-lineage-canvas__relation",
                                    isSelected ? "is-selected" : "",
                                    isAdjacent ? "" : "is-muted"
                                ].join(" ")}
                                key={relation.id || relation.relationId}
                            >
                                <line
                                    x1={sourceNode.canvasX}
                                    y1={sourceNode.canvasY}
                                    x2={targetNode.canvasX}
                                    y2={targetNode.canvasY}
                                />
                                <line
                                    className="knowledge-lineage-canvas__relation-hit"
                                    x1={sourceNode.canvasX}
                                    y1={sourceNode.canvasY}
                                    x2={targetNode.canvasX}
                                    y2={targetNode.canvasY}
                                    onClick={(event) => {
                                        event.stopPropagation();
                                        onSelectRelation(relation);
                                    }}
                                />
                                <text
                                    x={labelX}
                                    y={labelY}
                                    onClick={(event) => {
                                        event.stopPropagation();
                                        onSelectRelation(relation);
                                    }}
                                >
                                    {readRelationLabel(relation)}
                                </text>
                            </g>
                        );
                    })}
                </g>
                <g className="knowledge-lineage-canvas__nodes">
                    {canvasNodes.map((node) => {
                        const isSelected = node.nodeId === selectedNodeId;
                        const isMuted =
                            selectedNodeId != null && !selectedNeighborNodeIds.has(node.nodeId);
                        return (
                            <g
                                className={[
                                    "knowledge-lineage-canvas__node",
                                    readStatusClassName(node.confirmationStatus),
                                    isSelected ? "is-selected" : "",
                                    isMuted ? "is-muted" : ""
                                ].join(" ")}
                                key={node.id || node.nodeId}
                                role="button"
                                tabIndex={0}
                                transform={`translate(${node.canvasX - NODE_WIDTH / 2} ${node.canvasY - NODE_HEIGHT / 2})`}
                                onClick={(event) => {
                                    event.stopPropagation();
                                    onSelectNode(node);
                                }}
                                onKeyDown={(event) => {
                                    if (event.key === "Enter" || event.key === " ") {
                                        event.preventDefault();
                                        onSelectNode(node);
                                    }
                                }}
                            >
                                <rect height={NODE_HEIGHT} rx={8} width={NODE_WIDTH} />
                                <text className="knowledge-lineage-canvas__node-name" x={14} y={24}>
                                    {formatNodeName(node)}
                                </text>
                                <text className="knowledge-lineage-canvas__node-meta" x={14} y={44}>
                                    {node.nodeType || "未分类"} / {node.generation ?? "-"}
                                </text>
                            </g>
                        );
                    })}
                </g>
            </svg>
        </div>
    );
};
