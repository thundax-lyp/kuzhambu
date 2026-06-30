import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { RefinementPage } from "./refinement-page";

const componentMocks = vi.hoisted(() => ({
    mockRefinementEntityEditor: () => null,
    mockRefinementEntityDeleteModal: () => null,
    mockRefinementRelationEditor: () => null,
    mockRefinementRelationDeleteModal: () => null,
    mockRefinementFilterForm: () => <div>筛选器</div>,
    mockRefinementProgressSummaryPanel: () => <div>进度摘要</div>,
    mockRefinementWorkbenchTable: ({
        items
    }: {
        items: Array<{ refinementTaskId: number; sourceContentType: string }>;
    }) => (
        <div aria-label="知识图谱精修任务表格">
            {items.map((item) => (
                <div key={item.refinementTaskId}>{item.sourceContentType}</div>
            ))}
        </div>
    ),
    mockRefinementEntityTable: () => null,
    mockRefinementRelationTable: () => null
}));

vi.mock("./components/refinement-entity-editor", () => ({
    RefinementEntityEditor: componentMocks.mockRefinementEntityEditor
}));
vi.mock("./components/refinement-entity-delete-modal", () => ({
    RefinementEntityDeleteModal: componentMocks.mockRefinementEntityDeleteModal
}));
vi.mock("./components/refinement-relation-editor", () => ({
    RefinementRelationEditor: componentMocks.mockRefinementRelationEditor
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
    getTaskDraft: vi.fn(async () => null),
    addEntity: vi.fn(async () => ({})),
    addRelation: vi.fn(async () => ({})),
    applyTask: vi.fn(async () => ({})),
    confirmEntity: vi.fn(async () => ({})),
    confirmRelation: vi.fn(async () => ({})),
    deleteEntity: vi.fn(async () => undefined),
    deleteRelation: vi.fn(async () => undefined),
    updateEntity: vi.fn(async () => ({})),
    updateRelation: vi.fn(async () => ({}))
}));

describe("RefinementPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["knowledge:refinement:view", "knowledge:refinement:edit"]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
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

        expect(
            await screen.findByRole("heading", { name: "知识图谱精修工作台" })
        ).toBeInTheDocument();
        expect(await screen.findByLabelText("知识图谱精修任务表格")).toBeInTheDocument();
        expect(screen.getByText("SANCAI_ENTRY")).toBeInTheDocument();
    }, 10000);
});
