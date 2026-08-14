import { useEffect, useMemo, useState } from "react";
import { KuzhambuAlert, KuzhambuCard, KuzhambuGraph, KuzhambuSpace } from "@/components";
import type { KuzhambuGraphSpoItem } from "@/components";
import type { GraphWorkbenchEdgeRecord, GraphWorkbenchNodeRecord } from "../graph-workbench-types";

interface WorkbenchCanvasProps {
    edgeBatches: readonly {
        nodes: readonly GraphWorkbenchNodeRecord[];
        edges: readonly GraphWorkbenchEdgeRecord[];
    }[];
    seedNodes: readonly GraphWorkbenchNodeRecord[];
    onSelectNode: (node: GraphWorkbenchNodeRecord) => void;
}

const toSpoItems = (
    seedNodes: readonly GraphWorkbenchNodeRecord[],
    edges: readonly GraphWorkbenchEdgeRecord[]
): KuzhambuGraphSpoItem[] => {
    const nodeLabels = new Map(seedNodes.map((node) => [node.id, node.label]));
    return edges.map((edge) => ({
        subject: nodeLabels.get(edge.source) ?? edge.source,
        predicate: edge.predicate,
        object: nodeLabels.get(edge.target) ?? edge.target,
        subjectGroup: "图谱节点",
        objectGroup: "图谱节点"
    }));
};

export const WorkbenchCanvas = ({ edgeBatches, seedNodes, onSelectNode }: WorkbenchCanvasProps) => {
    const [loadedBatchCount, setLoadedBatchCount] = useState(0);
    const [isComplete, setIsComplete] = useState(false);

    useEffect(() => {
        const timers = edgeBatches.map((_, index) =>
            window.setTimeout(
                () => {
                    setLoadedBatchCount(index + 1);
                    if (index === edgeBatches.length - 1) {
                        setIsComplete(true);
                    }
                },
                (index + 1) * 80
            )
        );
        return () => timers.forEach((timer) => window.clearTimeout(timer));
    }, [edgeBatches]);

    const loadedEdges = useMemo(
        () => edgeBatches.slice(0, loadedBatchCount).flatMap((batch) => batch.edges),
        [edgeBatches, loadedBatchCount]
    );
    const loadedNodeIds = useMemo(
        () => new Set(loadedEdges.flatMap((edge) => [edge.source, edge.target])),
        [loadedEdges]
    );
    const visibleNodes = seedNodes.filter(
        (node) => !isComplete || !node.isOrphan || loadedNodeIds.has(node.id)
    );
    const spoItems = useMemo(() => toSpoItems(seedNodes, loadedEdges), [seedNodes, loadedEdges]);

    return (
        <KuzhambuCard
            title="渐进局部画布"
            extra={<span>{isComplete ? "加载完成" : `正在加载第 ${loadedBatchCount + 1} 批`}</span>}
        >
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <KuzhambuAlert
                    title={
                        isComplete
                            ? "边批次加载完成，已移除孤立节点"
                            : "种子节点已淡化，等待边批次返回"
                    }
                    type={isComplete ? "success" : "info"}
                    showIcon
                />
                <KuzhambuGraph height={360} spoList={spoItems} />
                <KuzhambuSpace wrap>
                    {visibleNodes.map((node) => (
                        <GraphNodeButton
                            key={node.id}
                            isFaded={!isComplete && node.isFaded}
                            label={node.label}
                            onClick={() => onSelectNode(node)}
                        />
                    ))}
                </KuzhambuSpace>
                <span aria-label="局部画布节点数量">当前节点 {visibleNodes.length} / 最多 200</span>
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};

interface GraphNodeButtonProps {
    isFaded: boolean;
    label: string;
    onClick: () => void;
}

const GraphNodeButton = ({ isFaded, label, onClick }: GraphNodeButtonProps) => (
    <button aria-label={`查看节点 ${label}`} data-faded={isFaded} type="button" onClick={onClick}>
        {label}
    </button>
);
