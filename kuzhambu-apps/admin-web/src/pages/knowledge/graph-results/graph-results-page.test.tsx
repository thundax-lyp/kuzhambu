import { cleanup, render, screen } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { GraphResultsPage } from "./graph-results-page";

vi.mock("./graph-results-service", () => ({
    pageVersions: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 1,
        totalPage: 1,
        count: 1,
        records: [
            {
                versionId: 71,
                taskId: "31",
                taskType: "GRAPH",
                status: "APPLIED",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                versionNo: 2
            }
        ]
    })),
    getVersionDetail: vi.fn(async () => ({
        versionId: 71,
        taskId: "31",
        taskType: "GRAPH",
        status: "APPLIED",
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        versionNo: 2
    })),
    pageEntities: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    getEntityDetail: vi.fn(async () => null),
    pageRelations: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    getRelationDetail: vi.fn(async () => null),
    pageLineageNodes: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    getLineageNodeDetail: vi.fn(async () => null),
    pageLineageRelations: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    getLineageRelationDetail: vi.fn(async () => null)
}));

describe("GraphResultsPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["knowledge:graph:view"]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
        queryClient.clear();
        cleanup();
    });

    it("renders graph version list entry", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <GraphResultsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(screen.getByRole("heading", { level: 2, name: "正式结果读取" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { level: 4, name: "结果入口" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "图谱版本" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "正式实体" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "正式关系" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "正式世系" })).toBeInTheDocument();
        expect(await screen.findByLabelText("知识图谱版本表格")).toBeInTheDocument();
        expect(screen.getByText("71")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "查看详情" })).toBeInTheDocument();
        expect(
            screen.getByText(
                "当前页已以图谱版本作为主入口，管理员可以从版本详情下钻审阅实体、关系和世系正式结果。"
            )
        ).toBeInTheDocument();
    });
});
