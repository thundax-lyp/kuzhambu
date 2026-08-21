import { Network, Search } from "lucide-react";
import { useMemo, useState } from "react";
import { KnowledgeGraphCanvas } from "@/components/knowledge-graph-canvas";
import {
    readKnowledgeGraphNodeTypeLabel,
    readKnowledgeGraphRelationLabel
} from "@/components/knowledge-graph-labels";
import type {
    SancaiGraphEdgeRecord,
    SancaiGraphNodeRecord,
    SancaiGraphRecord
} from "./sancai-types";

import "./sancai-graph-view.css";

const readQualifierValue = (value: unknown) => {
    if (typeof value === "string" || typeof value === "number" || typeof value === "boolean") {
        return String(value);
    }
    return JSON.stringify(value);
};

const SancaiGraphInspector = ({
    edges,
    nodes,
    selectedNode
}: {
    edges: SancaiGraphEdgeRecord[];
    nodes: SancaiGraphNodeRecord[];
    selectedNode: SancaiGraphNodeRecord | null;
}) => {
    if (!selectedNode) {
        return (
            <aside className="sancai-graph-inspector is-empty" aria-label="图谱对象详情">
                <Network aria-hidden="true" size={22} />
                <h3>选择一个对象</h3>
                <p>点击画布中的节点，查看它在本稿件中的相关关系。</p>
            </aside>
        );
    }

    const nodesById = new Map(nodes.map((node) => [node.id, node]));
    const relatedEdges = edges.filter(
        (edge) => edge.sourceNodeId === selectedNode.id || edge.targetNodeId === selectedNode.id
    );

    return (
        <aside className="sancai-graph-inspector" aria-label="图谱对象详情">
            <p className="sancai-graph-inspector-kicker">
                {readKnowledgeGraphNodeTypeLabel(selectedNode.nodeType)}
            </p>
            <h3>{selectedNode.name}</h3>
            <div className="sancai-graph-inspector-section">
                <h4>相关关系</h4>
                {relatedEdges.length ? (
                    <ul>
                        {relatedEdges.map((edge) => {
                            const sourceNode = nodesById.get(edge.sourceNodeId);
                            const targetNode = nodesById.get(edge.targetNodeId);
                            const sourceName = sourceNode?.name || "未知对象";
                            const targetName = targetNode?.name || "未知对象";
                            const displayedRelation = readKnowledgeGraphRelationLabel(
                                edge.relationType
                            );
                            const qualifierEntries = Object.entries(edge.qualifiers || {});
                            return (
                                <li key={edge.id}>
                                    <p
                                        className="sancai-graph-relation-statement"
                                        aria-label={`${sourceName} ${displayedRelation} ${targetName}`}
                                    >
                                        <b
                                            className={
                                                edge.sourceNodeId === selectedNode.id
                                                    ? "is-selected"
                                                    : ""
                                            }
                                        >
                                            {sourceName}
                                        </b>
                                        <strong>{displayedRelation}</strong>
                                        <b
                                            className={
                                                edge.targetNodeId === selectedNode.id
                                                    ? "is-selected"
                                                    : ""
                                            }
                                        >
                                            {targetName}
                                        </b>
                                    </p>
                                    {qualifierEntries.length ? (
                                        <dl>
                                            {qualifierEntries.map(([key, value]) => (
                                                <div key={key}>
                                                    <dt>{key}</dt>
                                                    <dd>{readQualifierValue(value)}</dd>
                                                </div>
                                            ))}
                                        </dl>
                                    ) : null}
                                </li>
                            );
                        })}
                    </ul>
                ) : (
                    <p>当前对象在本稿件中没有关联关系。</p>
                )}
            </div>
        </aside>
    );
};

export const SancaiGraphView = ({ graph }: { graph: SancaiGraphRecord }) => {
    const [searchKeyword, setSearchKeyword] = useState("");
    const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
    const selectedNode = graph.nodes.find((node) => node.id === selectedNodeId) || null;
    const canvasGraph = useMemo(
        () => ({ edges: graph.edges, nodes: graph.nodes }),
        [graph.edges, graph.nodes]
    );

    if (!graph.visible || graph.nodes.length === 0) {
        return (
            <section className="sancai-graph-empty" aria-label="稿件知识图谱">
                <Network aria-hidden="true" size={28} />
                <h3>本稿件暂无已发布图谱</h3>
                <p>图谱整理完成并发布后将在这里展示。</p>
            </section>
        );
    }

    return (
        <section className="sancai-graph-view" aria-label="稿件知识图谱">
            <header className="sancai-graph-toolbar">
                <div>
                    <strong>{graph.nodes.length} 个对象</strong>
                    <span>{graph.edges.length} 条关系</span>
                </div>
                <label>
                    <Search aria-hidden="true" size={15} />
                    <span className="sr-only">查找图谱对象</span>
                    <input
                        aria-label="查找图谱对象"
                        placeholder="查找图谱对象"
                        type="search"
                        value={searchKeyword}
                        onChange={(event) => setSearchKeyword(event.target.value)}
                    />
                </label>
            </header>
            <div className="sancai-graph-layout">
                <div className="sancai-graph-canvas">
                    <KnowledgeGraphCanvas
                        ariaLabel="稿件关系画布"
                        graph={canvasGraph}
                        searchKeyword={searchKeyword}
                        selectedNodeId={selectedNodeId}
                        onNodeClick={setSelectedNodeId}
                    />
                </div>
                <SancaiGraphInspector
                    edges={graph.edges}
                    nodes={graph.nodes}
                    selectedNode={selectedNode}
                />
            </div>
        </section>
    );
};
