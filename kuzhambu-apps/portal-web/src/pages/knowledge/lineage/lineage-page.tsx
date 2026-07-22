import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, GitBranch, Search, UserRound } from "lucide-react";
import { useState } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { LineageFlowCanvas } from "./components/lineage-flow-canvas";
import * as KnowledgeLineageService from "./lineage-service";
import type { KnowledgeLineageCanvasQuery } from "./lineage-service";
import type { KnowledgeLineageCanvasRecord } from "./lineage-types";

import "@xyflow/react/dist/style.css";
import "./lineage-page.css";

const EMPTY_CANVAS: KnowledgeLineageCanvasRecord = {
    summary: {
        nodeCount: 0,
        relationCount: 0,
        confirmedNodeCount: 0,
        confirmedRelationCount: 0
    },
    nodes: [],
    relations: [],
    selectedNode: null,
    selectedRelation: null,
    availableFilters: {
        versions: [],
        nodeTypes: [],
        relationTypes: [],
        confirmationStatuses: []
    },
    empty: {
        reason: "NO_LINEAGE_DATA",
        title: "暂无世系图",
        description: "当前还没有可浏览的正式世系数据。"
    }
};

const readVersionLabel = (
    versionId: number | null | undefined,
    canvas: KnowledgeLineageCanvasRecord
) => {
    const version =
        canvas.availableFilters.versions.find((item) => item.versionId === versionId) ??
        canvas.version;
    if (!version) {
        return "最新已应用版本";
    }
    const versionText = version.versionNo == null ? version.versionId : version.versionNo;
    return `版本 ${versionText} / ${version.sourceCategoryName || version.sourceContentType || "世系"}`;
};

const renderSourceRefs = (
    sourceRefs: { sourceTitle?: string | null; snippet?: string | null; href?: string | null }[]
) => {
    if (sourceRefs.length === 0) {
        return <p className="knowledge-lineage-muted">暂无来源摘录</p>;
    }
    return (
        <ul className="knowledge-lineage-source-list">
            {sourceRefs.map((sourceRef, index) => (
                <li key={`${sourceRef.sourceTitle || "source"}-${index}`}>
                    <strong>{sourceRef.sourceTitle || "来源"}</strong>
                    {sourceRef.snippet ? <span>{sourceRef.snippet}</span> : null}
                    {sourceRef.href ? (
                        <a href={sourceRef.href} target="_blank" rel="noreferrer">
                            打开
                        </a>
                    ) : null}
                </li>
            ))}
        </ul>
    );
};

export const KnowledgeLineagePage = () => {
    const [query, setQuery] = useState<KnowledgeLineageCanvasQuery>({
        versionId: null,
        focusNodeId: null,
        focusRelationId: null,
        keyword: null,
        nodeType: null,
        relationType: null,
        confirmationStatus: null,
        depth: 2
    });
    const [keywordInput, setKeywordInput] = useState("");
    const lineageQuery = useQuery({
        queryFn: () => KnowledgeLineageService.getKnowledgeLineage(query),
        queryKey: ["knowledge-lineage", query]
    });
    const canvas = lineageQuery.data ?? EMPTY_CANVAS;
    const selectedNode = canvas.selectedNode;
    const selectedRelation = canvas.selectedRelation;
    const emptyText = canvas.empty?.description || canvas.empty?.title;

    const updateQuery = (patch: Partial<KnowledgeLineageCanvasQuery>) => {
        setQuery((current) => ({
            ...current,
            ...patch
        }));
    };
    const clearFilters = () => {
        setKeywordInput("");
        setQuery((current) => ({
            versionId: current.versionId ?? null,
            focusNodeId: null,
            focusRelationId: null,
            keyword: null,
            nodeType: null,
            relationType: null,
            confirmationStatus: null,
            depth: 2
        }));
    };
    let detailContent = (
        <>
            <p>详情</p>
            <h2>请选择节点或关系</h2>
            <span className="knowledge-lineage-muted">
                点击画布中的节点或关系后，这里会展示对应来源和确认状态。
            </span>
        </>
    );
    if (selectedNode) {
        detailContent = (
            <>
                <p>节点详情</p>
                <h2>{selectedNode.name || selectedNode.nodeKey}</h2>
                <dl>
                    <div>
                        <dt>类型</dt>
                        <dd>{selectedNode.nodeType || "-"}</dd>
                    </div>
                    <div>
                        <dt>代际</dt>
                        <dd>{selectedNode.generation ?? "-"}</dd>
                    </div>
                    <div>
                        <dt>确认状态</dt>
                        <dd>{selectedNode.confirmationStatus || "-"}</dd>
                    </div>
                </dl>
                {renderSourceRefs(selectedNode.sourceRefs)}
            </>
        );
    }
    if (selectedRelation) {
        detailContent = (
            <>
                <p>关系详情</p>
                <h2>{selectedRelation.relationLabel || selectedRelation.relationType}</h2>
                <dl>
                    <div>
                        <dt>起点</dt>
                        <dd>{selectedRelation.sourceNodeName || selectedRelation.sourceNodeId}</dd>
                    </div>
                    <div>
                        <dt>终点</dt>
                        <dd>{selectedRelation.targetNodeName || selectedRelation.targetNodeId}</dd>
                    </div>
                    <div>
                        <dt>确认状态</dt>
                        <dd>{selectedRelation.confirmationStatus || "-"}</dd>
                    </div>
                </dl>
                {renderSourceRefs(selectedRelation.sourceRefs)}
            </>
        );
    }

    return (
        <main className="knowledge-lineage-shell">
            <header className="knowledge-lineage-header">
                <div>
                    <p className="knowledge-lineage-kicker">Knowledge Lineage</p>
                    <h1>世系图浏览</h1>
                    <p>沿人物谱系、亲缘关系与来源摘录阅读正式世系结果。</p>
                </div>
                <Link className="knowledge-lineage-back" to="/knowledge">
                    <ArrowLeft size={16} />
                    返回知识馆
                </Link>
            </header>

            <section className="knowledge-lineage-filter" aria-label="世系筛选">
                <label>
                    <span>版本选择</span>
                    <select
                        value={query.versionId ?? ""}
                        onChange={(event) =>
                            updateQuery({
                                versionId: event.target.value ? Number(event.target.value) : null,
                                focusNodeId: null,
                                focusRelationId: null
                            })
                        }
                    >
                        <option value="">{readVersionLabel(query.versionId, canvas)}</option>
                        {canvas.availableFilters.versions.map((version) => (
                            <option key={version.versionId} value={version.versionId}>
                                {readVersionLabel(version.versionId, canvas)}
                            </option>
                        ))}
                    </select>
                </label>
                <form
                    className="knowledge-lineage-search"
                    onSubmit={(event) => {
                        event.preventDefault();
                        updateQuery({
                            keyword: keywordInput.trim() || null,
                            focusNodeId: null,
                            focusRelationId: null
                        });
                    }}
                >
                    <label>
                        <span>搜索</span>
                        <Input
                            value={keywordInput}
                            placeholder="搜索人物、谱系节点或关系"
                            onChange={(event) => setKeywordInput(event.target.value)}
                        />
                    </label>
                    <Button type="submit" variant="secondary">
                        <Search size={16} />
                        搜索
                    </Button>
                </form>
                <label>
                    <span>节点类型</span>
                    <select
                        value={query.nodeType ?? ""}
                        onChange={(event) =>
                            updateQuery({
                                nodeType: event.target.value || null,
                                focusNodeId: null,
                                focusRelationId: null
                            })
                        }
                    >
                        <option value="">全部节点</option>
                        {canvas.availableFilters.nodeTypes.map((nodeType) => (
                            <option key={nodeType} value={nodeType}>
                                {nodeType}
                            </option>
                        ))}
                    </select>
                </label>
                <label>
                    <span>关系类型</span>
                    <select
                        value={query.relationType ?? ""}
                        onChange={(event) =>
                            updateQuery({
                                relationType: event.target.value || null,
                                focusNodeId: null,
                                focusRelationId: null
                            })
                        }
                    >
                        <option value="">全部关系</option>
                        {canvas.availableFilters.relationTypes.map((relationType) => (
                            <option key={relationType} value={relationType}>
                                {relationType}
                            </option>
                        ))}
                    </select>
                </label>
                <Button type="button" variant="outline" onClick={clearFilters}>
                    清除筛选
                </Button>
            </section>

            <section className="knowledge-lineage-layout">
                <section className="knowledge-lineage-stage" aria-label="世系画布">
                    <div className="knowledge-lineage-stage-head">
                        <div>
                            <p>{readVersionLabel(query.versionId, canvas)}</p>
                            <h2>{canvas.version?.sourceCategoryName || "正式世系画布"}</h2>
                        </div>
                        <div className="knowledge-lineage-metrics">
                            <span>
                                <UserRound size={16} />
                                节点 {canvas.summary.nodeCount}
                            </span>
                            <span>
                                <GitBranch size={16} />
                                关系 {canvas.summary.relationCount}
                            </span>
                        </div>
                    </div>

                    <LineageFlowCanvas
                        canvas={canvas}
                        emptyText={emptyText}
                        fetching={lineageQuery.isFetching}
                        selectedNodeId={query.focusNodeId}
                        selectedRelationId={query.focusRelationId}
                        onSelectNode={(nodeId) =>
                            updateQuery({
                                focusNodeId: nodeId,
                                focusRelationId: null
                            })
                        }
                        onSelectRelation={(relationId) =>
                            updateQuery({
                                focusNodeId: null,
                                focusRelationId: relationId
                            })
                        }
                    />
                </section>

                <aside className="knowledge-lineage-detail" aria-label="世系详情">
                    {detailContent}
                </aside>
            </section>
        </main>
    );
};
