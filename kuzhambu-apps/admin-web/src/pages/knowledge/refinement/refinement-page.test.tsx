import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { RefinementPage } from "./refinement-page";
import * as service from "./refinement-service";

const componentMocks = vi.hoisted(() => ({
    mockRefinementEntityEditModal: () => null,
    mockRefinementEntityDeleteModal: () => null,
    mockRefinementRelationEditModal: () => null,
    mockRefinementRelationDeleteModal: () => null,
    mockRefinementFilterForm: () => <div>筛选器</div>,
    mockRefinementProgressSummaryPanel: () => <div>进度摘要</div>,
    mockRefinementWorkbenchTable: ({
        items,
        onOpenTask
    }: {
        items: Array<{ refinementTaskId: number; sourceContentType: string }>;
        onOpenTask: (item: { refinementTaskId: number; graphVersionId: number }) => void;
    }) => (
        <div aria-label="知识图谱精修任务表格">
            {items.map((item) => (
                <button
                    key={item.refinementTaskId}
                    type="button"
                    onClick={() => onOpenTask({ ...item, graphVersionId: 71 })}
                >
                    {item.sourceContentType}
                </button>
            ))}
        </div>
    ),
    mockRefinementEntityTable: () => null,
    mockRefinementRelationTable: () => null
}));

vi.mock("./components/refinement-entity-edit-modal", () => ({
    RefinementEntityEditModal: componentMocks.mockRefinementEntityEditModal
}));
vi.mock("./components/refinement-entity-delete-modal", () => ({
    RefinementEntityDeleteModal: componentMocks.mockRefinementEntityDeleteModal
}));
vi.mock("./components/refinement-relation-edit-modal", () => ({
    RefinementRelationEditModal: componentMocks.mockRefinementRelationEditModal
}));
vi.mock("./components/refinement-relation-delete-modal", () => ({
    RefinementRelationDeleteModal: componentMocks.mockRefinementRelationDeleteModal
}));
vi.mock("./components/refinement-filter-form", () => ({
    RefinementFilterForm: componentMocks.mockRefinementFilterForm
}));
vi.mock("./components/refinement-progress-summary", () => ({
    RefinementProgressSummaryPanel: componentMocks.mockRefinementProgressSummaryPanel
}));
vi.mock("./components/refinement-workbench-table", () => ({
    RefinementWorkbenchTable: componentMocks.mockRefinementWorkbenchTable
}));
vi.mock("./components/refinement-entity-table", () => ({
    RefinementEntityTable: componentMocks.mockRefinementEntityTable
}));
vi.mock("./components/refinement-relation-table", () => ({
    RefinementRelationTable: componentMocks.mockRefinementRelationTable
}));

vi.mock("./refinement-service", () => ({
    pageTasks: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 1,
        totalPage: 1,
        count: 1,
        records: [
            {
                refinementTaskId: 31,
                graphVersionId: 71,
                taskType: "GRAPH",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                sourceCategoryCode: "myth",
                sourceCategoryName: "神话",
                status: "DRAFT",
                progressSummary: {
                    entityPendingCount: 1,
                    entityConfirmedCount: 0,
                    relationPendingCount: 1,
                    relationConfirmedCount: 0
                }
            }
        ]
    })),
    getQualitySummary: vi.fn(async () => ({
        entityCoverageRate: 0.8,
        relationAccuracyRate: 0.75,
        completenessRate: 0.77
    })),
    getTaskDetail: vi.fn(async () => null),
    getTaskDraft: vi.fn(async () => ({
        refinementTaskId: 31,
        graphVersionId: 71,
        taskType: "GRAPH",
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        sourceCategoryCode: "myth",
        sourceCategoryName: "神话",
        status: "DRAFT",
        progressSummary: {
            entityPendingCount: 1,
            entityConfirmedCount: 0,
            relationPendingCount: 1,
            relationConfirmedCount: 0
        },
        entities: [],
        relations: [],
        lineageNodes: [],
        lineageRelations: [],
        entityOptions: []
    })),
    addEntity: vi.fn(async () => ({})),
    addRelation: vi.fn(async () => ({})),
    applyTask: vi.fn(async () => ({
        refinementTaskId: 31,
        graphVersionId: 71,
        taskType: "GRAPH",
        sourceTaskId: 88,
        selectionScopeJson: '{"sourceContentIds":[1001]}',
        replaceUnconfirmedOnly: true,
        triggerSource: "REFINEMENT_APPLIED",
        nextAction: "OPEN_GRAPH_VERSION",
        qualityReportRefreshRequired: true,
        status: "APPLIED"
    })),
    confirmEntity: vi.fn(async () => ({})),
    confirmRelation: vi.fn(async () => ({})),
    deleteEntity: vi.fn(async () => undefined),
    deleteRelation: vi.fn(async () => undefined),
    updateEntity: vi.fn(async () => ({})),
    updateRelation: vi.fn(async () => ({})),
    pageAnnotations: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 200,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    }))
}));

vi.mock("@/service/current-user-service", () => ({
    getCurrentUserInfo: vi.fn(async () => ({
        id: "99",
        loginName: "admin"
    }))
}));

describe("RefinementPage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        queryClient.clear();
        window.history.pushState({}, "", "/");
        replacePermissions(["knowledge:refinement:view", "knowledge:refinement:edit"]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
        window.history.pushState({}, "", "/");
        queryClient.clear();
        cleanup();
    });

    it("renders page shell", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <RefinementPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "知识图谱工作台" })).toBeInTheDocument();
        expect(await screen.findByLabelText("知识图谱精修任务表格")).toBeInTheDocument();
        expect(screen.getByText("SANCAI_ENTRY")).toBeInTheDocument();
    }, 30000);

    it("shows graph follow-up actions after applying refinement", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <RefinementPage />
                </AntdApp>
            </QueryClientProvider>
        );

        fireEvent.click(await screen.findByRole("button", { name: "SANCAI_ENTRY" }));
        fireEvent.click(await screen.findByText("应用任务"));

        await waitFor(() => {
            expect(screen.getByRole("link", { name: "查看图谱结果" })).toHaveAttribute(
                "href",
                "/knowledge/graph-results?graphVersionId=71"
            );
        });
        expect(screen.getByRole("link", { name: "重生成图谱" })).toHaveAttribute(
            "href",
            "/knowledge/graph-extraction?regenerate=1&taskType=GRAPH&sourceTaskId=88&triggerSource=REFINEMENT_APPLIED&replaceUnconfirmedOnly=true&selectionScopeJson=%7B%22sourceContentIds%22%3A%5B1001%5D%7D"
        );
        expect(screen.getByRole("link", { name: "重新生成质量报告" })).toHaveAttribute(
            "href",
            "/knowledge/quality-report?graphVersionId=71&regenerate=1"
        );
    }, 30000);

    it("opens refinement task from graph version search param", async () => {
        window.history.pushState({}, "", "/knowledge/refinement?graphVersionId=71");

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <RefinementPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await waitFor(() => {
            expect(service.getTaskDraft).toHaveBeenCalled();
        });
        expect(vi.mocked(service.getTaskDraft).mock.calls[0]?.[0]).toEqual({
            graphVersionId: 71,
            openedBy: 99
        });
        expect(await screen.findByTestId("knowledge-refinement-task-drawer")).toBeInTheDocument();
    }, 30000);
});
