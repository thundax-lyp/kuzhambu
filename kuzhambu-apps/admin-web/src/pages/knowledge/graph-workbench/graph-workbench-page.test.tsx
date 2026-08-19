import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphWorkbenchPage } from "./graph-workbench-page";
import { useGraphWorkbenchAtlas } from "./hooks/use-graph-workbench-atlas";

const permissionState = vi.hoisted(() => ({
    permissions: new Set<string>()
}));

vi.mock("@/auth/permission-storage", () => ({
    hasPermission: (permission: string) => permissionState.permissions.has(permission),
    replacePermissions: (permissions: string[]) => {
        permissionState.permissions = new Set(permissions);
    }
}));

vi.mock("./hooks/use-graph-workbench-atlas", () => ({
    useGraphWorkbenchAtlas: vi.fn()
}));

vi.mock("./graph-workbench-canvas", () => {
    const MockGraphWorkbenchCanvas = ({
        graph
    }: {
        graph: { edges: unknown[]; nodes: unknown[] };
    }) => <div aria-label={`画布 ${graph.nodes.length}/${graph.edges.length}`} role="img" />;
    return { GraphWorkbenchCanvas: MockGraphWorkbenchCanvas };
});

const atlas = {
    graph: {
        edges: [{ id: "edge-1", sourceNodeId: "node-1", targetNodeId: "node-2" }],
        nodes: [
            { id: "node-1", name: "杜甫", nodeType: "PERSON" },
            { id: "node-2", name: "李白", nodeType: "PERSON" }
        ]
    },
    graphState: "ready" as const,
    onGraphLaidOut: () => undefined,
    overview: {
        coveredMaterialCount: "3",
        isolatedNodeCount: "1",
        missingCoreRelationNodeCount: "2",
        pendingConflictCount: "4",
        publishedEdgeCount: "6",
        publishedNodeCount: "5",
        recentActivities: [
            { occurredAt: "2026-08-19T00:00:00Z", summary: "发布杜甫与李白关系", type: "PUBLISH" }
        ],
        snapshotAt: "2026-08-19T00:00:00Z"
    },
    overviewState: "ready" as const
};

describe("GraphWorkbenchPage", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
        vi.clearAllMocks();
    });

    it("renders the read-only graph situation with its overview and activity", () => {
        replacePermissions(["knowledge:graph:view"]);
        vi.mocked(useGraphWorkbenchAtlas).mockReturnValue(atlas);

        render(<GraphWorkbenchPage />);

        expect(screen.getByRole("heading", { name: "图谱工作台" })).toBeInTheDocument();
        expect(screen.getByText("正式节点")).toBeInTheDocument();
        expect(screen.getByText("孤立节点")).toBeInTheDocument();
        expect(screen.queryByText("待决冲突")).not.toBeInTheDocument();
        expect(screen.queryByText("结构缺口")).not.toBeInTheDocument();
        expect(screen.getByText("发布杜甫与李白关系")).toBeInTheDocument();
        expect(screen.getByRole("img", { name: "画布 2/1" })).toBeInTheDocument();
        expect(screen.queryByRole("button")).not.toBeInTheDocument();
        expect(screen.queryByRole("table")).not.toBeInTheDocument();
    });

    it("shows the snapshot-unavailable state without hiding the graph", () => {
        replacePermissions(["knowledge:graph:view"]);
        vi.mocked(useGraphWorkbenchAtlas).mockReturnValue({
            ...atlas,
            overview: null,
            overviewState: "unavailable"
        });

        render(<GraphWorkbenchPage />);

        expect(screen.getByText("正式图态势正在准备")).toBeInTheDocument();
        expect(screen.getByRole("img", { name: "画布 2/1" })).toBeInTheDocument();
    });

    it("renders permission state without enabling the atlas", () => {
        replacePermissions([]);
        vi.mocked(useGraphWorkbenchAtlas).mockReturnValue(atlas);

        render(<GraphWorkbenchPage />);

        expect(screen.getByText("无权查看图谱工作台")).toBeInTheDocument();
        expect(useGraphWorkbenchAtlas).toHaveBeenCalledWith(false);
    });
});
