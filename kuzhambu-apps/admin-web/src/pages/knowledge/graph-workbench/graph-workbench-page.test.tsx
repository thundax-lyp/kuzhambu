import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphWorkbenchPage } from "./graph-workbench-page";
import * as service from "./graph-workbench-service";

const permissionState = vi.hoisted(() => ({
    permissions: new Set<string>()
}));

vi.mock("@/auth/permission-storage", () => ({
    hasPermission: (permission: string) => permissionState.permissions.has(permission),
    replacePermissions: (permissions: string[]) => {
        permissionState.permissions = new Set(permissions);
    }
}));

vi.mock("./graph-workbench-service", () => ({
    pagePublishedAdjacency: vi.fn()
}));

const adjacencyPage = {
    count: 2,
    pageNo: 1,
    pageSize: 20,
    records: [
        {
            isolated: false,
            object: {
                id: "2",
                name: "李白",
                nodeType: "PERSON",
                source: "MATERIAL",
                status: "ACTIVE"
            },
            relation: {
                id: "10",
                relationType: "MENTIONS",
                source: "MATERIAL",
                status: "ACTIVE"
            },
            subject: {
                id: "1",
                name: "杜甫",
                nodeType: "PERSON",
                source: "MATERIAL",
                status: "ACTIVE"
            }
        },
        {
            isolated: true,
            object: null,
            relation: null,
            subject: {
                id: "3",
                name: "孤立节点",
                nodeType: "CONCEPT",
                source: "MANUAL",
                status: "ACTIVE"
            }
        }
    ],
    totalCount: 2,
    totalPage: 1
};

const createTestQueryClient = () =>
    new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

const renderWorkbench = () =>
    render(
        <QueryClientProvider client={createTestQueryClient()}>
            <MemoryRouter>
                <GraphWorkbenchPage />
            </MemoryRouter>
        </QueryClientProvider>
    );

describe("GraphWorkbenchPage", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
        vi.clearAllMocks();
    });

    it("renders the published adjacency table with isolated nodes", async () => {
        replacePermissions(["knowledge:graph:view"]);
        vi.mocked(service.pagePublishedAdjacency).mockResolvedValue(adjacencyPage);

        renderWorkbench();

        expect(screen.getByRole("heading", { name: "图谱工作台" })).toBeInTheDocument();
        expect(await screen.findByText("杜甫")).toBeInTheDocument();
        expect(screen.getByText("MENTIONS")).toBeInTheDocument();
        expect(screen.getByText("李白")).toBeInTheDocument();
        expect(
            screen.getByRole("row", { name: "孤立节点 CONCEPT - - - 孤立节点 MANUAL" })
        ).toBeInTheDocument();
        expect(screen.getByRole("columnheader", { name: "主语" })).toBeInTheDocument();
    });

    it("submits filters to the adjacency service", async () => {
        replacePermissions(["knowledge:graph:view"]);
        vi.mocked(service.pagePublishedAdjacency).mockResolvedValue(adjacencyPage);
        const user = userEvent.setup();

        renderWorkbench();
        await screen.findByText("杜甫");
        await user.type(screen.getByRole("textbox", { name: "筛选主语关键词" }), "杜甫");
        await user.type(screen.getByRole("textbox", { name: "筛选谓词" }), "MENTIONS");
        await user.type(screen.getByRole("textbox", { name: "筛选宾语关键词" }), "李白");
        await user.click(screen.getByTestId("knowledge-graph-workbench-apply-filters-button"));

        await waitFor(() =>
            expect(vi.mocked(service.pagePublishedAdjacency).mock.calls.at(-1)?.[0]).toMatchObject({
                includeIsolated: true,
                objectKeyword: "李白",
                pageNo: 1,
                pageSize: 20,
                relationStatus: "ACTIVE",
                relationType: "MENTIONS",
                subjectKeyword: "杜甫",
                subjectStatus: "ACTIVE"
            })
        );
    });

    it("renders permission state without querying data", () => {
        replacePermissions([]);

        renderWorkbench();

        expect(screen.getByText("无权查看图谱工作台")).toBeInTheDocument();
        expect(vi.mocked(service.pagePublishedAdjacency)).not.toHaveBeenCalled();
    });
});
