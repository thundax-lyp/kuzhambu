import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { afterEach, describe, expect, it, vi } from "vitest";
import { clearPermissions, replacePermissions } from "@/auth/permission-storage";
import { LineagePage } from "./lineage-page";
import type { LineageCanvasQuery } from "./lineage-service";
import type {
    LineageCanvasRecord,
    LineageNodeRecord,
    LineageRelationRecord
} from "./lineage-types";

const { getLineageCanvas } = vi.hoisted(() => ({
    getLineageCanvas: vi.fn()
}));

vi.mock("./lineage-service", () => ({
    getLineageCanvas
}));

/* eslint-disable @typescript-eslint/naming-convention */
vi.mock("./components/lineage-filter-bar", () => ({
    LineageFilterBar: ({
        filters,
        onChange,
        onRefresh,
        onReset,
        query
    }: {
        filters: { nodeTypes: string[]; relationTypes: string[]; confirmationStatuses: string[] };
        onChange: (query: LineageCanvasQuery) => void;
        onRefresh: () => void;
        onReset: () => void;
        query: LineageCanvasQuery;
    }) => (
        <div aria-label="mock-filter-bar">
            <span>{filters.nodeTypes.join(",")}</span>
            <span>{filters.relationTypes.join(",")}</span>
            <span>{filters.confirmationStatuses.join(",")}</span>
            <button
                type="button"
                onClick={() =>
                    onChange({
                        ...query,
                        keyword: "贾",
                        focusNodeId: null,
                        focusRelationId: null
                    })
                }
            >
                搜索世系节点或关系
            </button>
            <button
                type="button"
                onClick={() =>
                    onChange({
                        ...query,
                        nodeType: "PERSON",
                        focusNodeId: null,
                        focusRelationId: null
                    })
                }
            >
                节点类型
            </button>
            <button type="button" onClick={onReset}>
                重置
            </button>
            <button type="button" onClick={onRefresh}>
                刷新
            </button>
        </div>
    )
}));

vi.mock("./components/lineage-canvas", () => ({
    LineageCanvas: ({
        nodes,
        onSelectNode,
        onSelectRelation,
        relations
    }: {
        nodes: LineageNodeRecord[];
        onSelectNode: (node: LineageNodeRecord) => void;
        onSelectRelation: (relation: LineageRelationRecord) => void;
        relations: LineageRelationRecord[];
    }) => (
        <div aria-label="mock-lineage-canvas">
            {nodes.map((node) => (
                <button key={node.nodeId} type="button" onClick={() => onSelectNode(node)}>
                    画布节点 {node.name}
                </button>
            ))}
            {relations.map((relation) => (
                <button
                    key={relation.relationId}
                    type="button"
                    onClick={() => onSelectRelation(relation)}
                >
                    画布关系 {relation.relationLabel}
                </button>
            ))}
        </div>
    )
}));

vi.mock("./components/lineage-node-table", () => ({
    LineageNodeTable: ({
        nodes,
        onSelectNode
    }: {
        nodes: LineageNodeRecord[];
        onSelectNode: (node: LineageNodeRecord) => void;
    }) => (
        <div aria-label="mock-node-table">
            {nodes.map((node) => (
                <button key={node.nodeId} type="button" onClick={() => onSelectNode(node)}>
                    节点列表 {node.name}
                </button>
            ))}
        </div>
    )
}));

vi.mock("./components/lineage-relation-table", () => ({
    LineageRelationTable: ({
        onSelectRelation,
        relations
    }: {
        onSelectRelation: (relation: LineageRelationRecord) => void;
        relations: LineageRelationRecord[];
    }) => (
        <div aria-label="mock-relation-table">
            {relations.map((relation) => (
                <button
                    key={relation.relationId}
                    type="button"
                    onClick={() => onSelectRelation(relation)}
                >
                    关系列表 {relation.relationLabel}
                </button>
            ))}
        </div>
    )
}));

vi.mock("./components/lineage-detail-panel", () => ({
    LineageDetailPanel: ({
        node,
        relation
    }: {
        node?: LineageNodeRecord | null;
        relation?: LineageRelationRecord | null;
    }) => (
        <aside aria-label="mock-detail-panel">
            {node ? <span>节点详情 {node.name}</span> : null}
            {relation ? <span>关系详情 {relation.relationLabel}</span> : null}
        </aside>
    )
}));
/* eslint-enable @typescript-eslint/naming-convention */

const father: LineageNodeRecord = {
    id: "lineage-node:1",
    nodeId: 1,
    nodeKey: "person:1",
    name: "贾代善",
    nodeType: "PERSON",
    generation: 1,
    confirmationStatus: "CONFIRMED",
    sourceRefs: []
};

const son: LineageNodeRecord = {
    id: "lineage-node:2",
    nodeId: 2,
    nodeKey: "person:2",
    name: "贾政",
    nodeType: "PERSON",
    generation: 2,
    confirmationStatus: "CONFIRMED",
    sourceRefs: []
};

const relation: LineageRelationRecord = {
    id: "lineage-relation:10",
    relationId: 10,
    sourceNodeId: 1,
    sourceNodeName: "贾代善",
    targetNodeId: 2,
    targetNodeName: "贾政",
    relationType: "PARENT_CHILD",
    relationLabel: "父子",
    confirmationStatus: "CONFIRMED",
    sourceRefs: []
};

const canvasFor = (query: LineageCanvasQuery): LineageCanvasRecord => ({
    version: {
        versionId: 71,
        versionNo: 3,
        taskType: "LINEAGE",
        status: "APPLIED",
        sourceCategoryName: "红楼世系"
    },
    summary: {
        nodeCount: 2,
        relationCount: 1,
        confirmedNodeCount: 2,
        confirmedRelationCount: 1,
        focusNodeId: query.focusNodeId ?? null,
        focusRelationId: query.focusRelationId ?? null
    },
    nodes: [father, son],
    relations: [relation],
    selectedNode: query.focusNodeId === 2 ? son : null,
    selectedRelation: query.focusRelationId === 10 ? relation : null,
    availableFilters: {
        versions: [
            {
                versionId: 71,
                versionNo: 3,
                taskType: "LINEAGE",
                status: "APPLIED",
                sourceCategoryName: "红楼世系"
            }
        ],
        nodeTypes: ["PERSON"],
        relationTypes: ["PARENT_CHILD"],
        confirmationStatuses: ["CONFIRMED"]
    },
    empty: null
});

const renderPage = () => {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                gcTime: 0,
                retry: false
            }
        }
    });

    return render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <LineagePage />
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("LineagePage", () => {
    afterEach(() => {
        cleanup();
        clearPermissions();
        getLineageCanvas.mockReset();
    });

    it("blocks lineage queries without graph view permission", async () => {
        renderPage();

        expect(await screen.findByText("当前账号暂无知识图谱查看权限。")).toBeInTheDocument();
        expect(getLineageCanvas).not.toHaveBeenCalled();
    });

    it("maps filters, canvas selection, list selection, reset and refresh to lineage queries", async () => {
        const user = userEvent.setup();
        replacePermissions(["knowledge:graph:view"]);
        getLineageCanvas.mockImplementation((query: LineageCanvasQuery) =>
            Promise.resolve(canvasFor(query))
        );

        renderPage();

        expect(await screen.findByRole("button", { name: "画布节点 贾政" })).toBeInTheDocument();
        expect(getLineageCanvas).toHaveBeenLastCalledWith(
            expect.objectContaining({
                depth: 2,
                focusNodeId: null,
                focusRelationId: null
            })
        );
        expect(screen.getByText("PERSON")).toBeInTheDocument();
        expect(screen.getByText("PARENT_CHILD")).toBeInTheDocument();
        expect(screen.getByText("CONFIRMED")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "搜索世系节点或关系" }));
        await waitFor(() =>
            expect(getLineageCanvas).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    keyword: "贾",
                    focusNodeId: null,
                    focusRelationId: null
                })
            )
        );

        await user.click(screen.getByRole("button", { name: "画布节点 贾政" }));
        await waitFor(() =>
            expect(getLineageCanvas).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    focusNodeId: 2,
                    focusRelationId: null
                })
            )
        );
        expect(await screen.findByText("节点详情 贾政")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "画布关系 父子" }));
        await waitFor(() =>
            expect(getLineageCanvas).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    focusNodeId: null,
                    focusRelationId: 10
                })
            )
        );
        expect(await screen.findByText("关系详情 父子")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "节点列表 贾政" }));
        await waitFor(() =>
            expect(getLineageCanvas).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    focusNodeId: 2,
                    focusRelationId: null
                })
            )
        );

        await user.click(screen.getByRole("button", { name: "重置" }));
        await waitFor(() =>
            expect(getLineageCanvas).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    keyword: null,
                    focusNodeId: null,
                    focusRelationId: null,
                    nodeType: null,
                    relationType: null,
                    confirmationStatus: null
                })
            )
        );

        const callCount = getLineageCanvas.mock.calls.length;
        await user.click(screen.getByRole("button", { name: "刷新" }));
        await waitFor(() => expect(getLineageCanvas.mock.calls.length).toBeGreaterThan(callCount));
    });
});
