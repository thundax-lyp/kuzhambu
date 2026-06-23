import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import * as service from "./refinement-service";
import { RefinementPage } from "./refinement-page";

const componentMocks = vi.hoisted(() => ({
    MockRefinementEntityEditor: () => null,
    MockRefinementEntityDeleteModal: () => null,
    MockRefinementRelationEditor: () => null,
    MockRefinementRelationDeleteModal: () => null,
    MockRefinementFilterForm: () => <div>筛选器</div>,
    MockRefinementProgressSummaryPanel: () => <div>进度摘要</div>,
    MockRefinementWorkbenchTable: ({
        items,
        onOpenTask
    }: {
        items: Array<{ refinementTaskId: number; sourceContentType: string }>;
        onOpenTask: (item: { refinementTaskId: number; sourceContentType: string }) => void;
    }) => (
        <div aria-label="知识图谱精修任务表格">
            {items.map((item) => (
                <div key={item.refinementTaskId}>
                    <span>{item.sourceContentType}</span>
                    <button type="button" onClick={() => onOpenTask(item)}>
                        打开任务
                    </button>
                </div>
            ))}
        </div>
    ),
    MockRefinementEntityTable: ({ entities = [] }: { entities?: Array<{ name: string }> }) => (
        <div>{entities.map((item) => item.name).join(",")}</div>
    ),
    MockRefinementRelationTable: ({
        relations = []
    }: {
        relations?: Array<{ sourceName: string; targetName: string }>;
    }) => <div>{relations.map((item) => `${item.sourceName}-${item.targetName}`).join(",")}</div>
}));

vi.mock("./components/refinement-entity-editor", () => ({
    RefinementEntityEditor: componentMocks.MockRefinementEntityEditor
}));

vi.mock("./components/refinement-entity-delete-modal", () => ({
    RefinementEntityDeleteModal: componentMocks.MockRefinementEntityDeleteModal
}));

vi.mock("./components/refinement-relation-editor", () => ({
    RefinementRelationEditor: componentMocks.MockRefinementRelationEditor
}));

vi.mock("./components/refinement-relation-delete-modal", () => ({
    RefinementRelationDeleteModal: componentMocks.MockRefinementRelationDeleteModal
}));

vi.mock("./components/refinement-filter-form", () => ({
    RefinementFilterForm: componentMocks.MockRefinementFilterForm
}));

vi.mock("./components/refinement-progress-summary", () => ({
    RefinementProgressSummaryPanel: componentMocks.MockRefinementProgressSummaryPanel
}));

vi.mock("./components/refinement-workbench-table", () => ({
    RefinementWorkbenchTable: componentMocks.MockRefinementWorkbenchTable
}));

vi.mock("./components/refinement-entity-table", () => ({
    RefinementEntityTable: componentMocks.MockRefinementEntityTable
}));

vi.mock("./components/refinement-relation-table", () => ({
    RefinementRelationTable: componentMocks.MockRefinementRelationTable
}));

vi.mock("./refinement-service", () => ({
    addEntity: vi.fn(async () => ({
        draftId: 401,
        entityKey: "manual:entity:01JTEST",
        name: "黄帝",
        entityType: "PERSON",
        confirmationStatus: "PENDING"
    })),
    addRelation: vi.fn(async () => ({
        draftId: 501,
        relationKey: "manual:relation:01JTEST",
        relationType: "ANCESTOR",
        confirmationStatus: "PENDING"
    })),
    applyTask: vi.fn(async () => ({
        refinementTaskId: 31,
        graphVersionId: 71,
        taskType: "GRAPH",
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        sourceCategoryCode: "myth",
        sourceCategoryName: "神话",
        status: "APPLIED",
        progressSummary: {
            entityPendingCount: 0,
            entityConfirmedCount: 1,
            relationPendingCount: 0,
            relationConfirmedCount: 1
        },
        entities: [],
        relations: [],
        lineageNodes: [],
        lineageRelations: [],
        entityOptions: []
    })),
    confirmEntity: vi.fn(async () => ({})),
    confirmRelation: vi.fn(async () => ({})),
    deleteEntity: vi.fn(async () => undefined),
    deleteRelation: vi.fn(async () => undefined),
    getTaskDetail: vi.fn(async () => ({
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
        entities: [
            {
                draftId: 11,
                entityId: 101,
                entityKey: "person:huangdi",
                name: "黄帝",
                entityType: "PERSON",
                confirmationStatus: "PENDING",
                operationType: "UPDATED"
            }
        ],
        relations: [
            {
                draftId: 12,
                relationId: 201,
                relationKey: "person:huangdi->person:fuxi:ancestor",
                sourceName: "黄帝",
                targetName: "伏羲",
                relationType: "ANCESTOR",
                confirmationStatus: "PENDING",
                operationType: "UPDATED"
            }
        ],
        lineageNodes: [],
        lineageRelations: [],
        entityOptions: [{ entityKey: "person:huangdi", name: "黄帝" }]
    })),
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
        entities: [
            {
                draftId: 11,
                entityId: 101,
                entityKey: "person:huangdi",
                name: "黄帝",
                entityType: "PERSON",
                confirmationStatus: "PENDING",
                operationType: "UPDATED"
            }
        ],
        relations: [
            {
                draftId: 12,
                relationId: 201,
                relationKey: "person:huangdi->person:fuxi:ancestor",
                sourceName: "黄帝",
                targetName: "伏羲",
                relationType: "ANCESTOR",
                confirmationStatus: "PENDING",
                operationType: "UPDATED"
            }
        ],
        lineageNodes: [],
        lineageRelations: [],
        entityOptions: [{ entityKey: "person:huangdi", name: "黄帝" }]
    })),
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

    it("renders tasks and opens a refinement detail", async () => {
        const user = userEvent.setup();
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <RefinementPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(
            screen.getByRole("heading", { level: 2, name: "知识图谱精修工作台" })
        ).toBeInTheDocument();
        expect(screen.getByRole("heading", { level: 4, name: "待精修任务" })).toBeInTheDocument();
        expect(await screen.findByLabelText("知识图谱精修任务表格")).toBeInTheDocument();
        expect(await screen.findByText("SANCAI_ENTRY")).toBeInTheDocument();

        await user.click(await screen.findByRole("button", { name: "打开任务" }));

        await waitFor(() => expect(service.getTaskDraft).toHaveBeenCalled());
        expect(await screen.findByText("实体草稿")).toBeInTheDocument();
        expect(screen.getByText("关系草稿")).toBeInTheDocument();
        expect(screen.getByText("黄帝")).toBeInTheDocument();
    });
});
