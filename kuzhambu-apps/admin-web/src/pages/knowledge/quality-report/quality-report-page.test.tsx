import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act, cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
import { QualityReportPage } from "./quality-report-page";
import * as service from "./quality-report-service";

const confirmDangerMock = vi.hoisted(() => vi.fn());
const qualityReportServiceMock = vi.hoisted(() => ({
    generateReport: vi.fn(async () => null),
    getLatestReport: vi.fn(async () => ({
        report: {
            reportId: 1001,
            reportNo: "KQR-1001",
            graphVersionId: 71,
            reportStatus: "PUBLISHED",
            entityCoverageRate: 0.9,
            relationAccuracyRate: 0.8,
            lineageCoverageRate: 0.7,
            completenessRate: 0.6
        },
        issues: [
            {
                issueId: 1,
                issueType: "ANNOTATION_ISSUE",
                severity: "high",
                objectType: "ENTITY",
                objectKey: "person:sunwukong",
                title: "实体需要复核"
            }
        ],
        sourceDetails: [
            {
                detailId: 1,
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                sourceCategoryCode: "myth",
                sourceCategoryName: "神话",
                graphVersionId: 71,
                issueCount: 2,
                status: "PUBLISHED"
            }
        ],
        annotations: [
            {
                annotationId: 1,
                objectType: "ENTITY",
                objectKey: "person:sunwukong",
                annotationStatus: "ISSUE",
                annotationLabel: "WRONG_ENTITY"
            }
        ]
    })),
    getReportDetail: vi.fn(async () => null),
    pageReports: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 1,
        totalPage: 1,
        count: 1,
        records: [
            {
                reportId: 1001,
                reportNo: "KQR-1001",
                graphVersionId: 71,
                reportStatus: "PUBLISHED",
                issueCount: 1
            }
        ]
    })),
    reextractLowQualityCategory: vi.fn(async () => ({
        reportId: 1001,
        sourceCategoryCode: "myth",
        sourceCategoryName: "神话",
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        taskId: 3001,
        batchJobId: 4001,
        taskType: "GRAPH",
        triggerSource: "QUALITY_REPORT",
        selectionScopeJson: '{"sourceContentIds":[1001]}',
        replaceUnconfirmedOnly: true
    }))
}));
const qualityReportSourceTableModuleMock = vi.hoisted(() => {
    const sourceTable = ({
        canReextract = false,
        reextractingDetailId = null,
        reportNo = null,
        sourceDetails = [],
        onReextract
    }: {
        canReextract?: boolean;
        reextractingDetailId?: number | null;
        reportNo?: string | null;
        sourceDetails?: {
            detailId: number;
            issueCount?: number | null;
            sourceCategoryCode?: string | null;
            sourceCategoryName?: string | null;
        }[];
        onReextract?: (sourceDetail: {
            detailId: number;
            issueCount?: number | null;
            sourceCategoryCode?: string | null;
            sourceCategoryName?: string | null;
        }) => void;
    }) => (
        <div aria-label="知识质量报告来源明细表格">
            {sourceDetails.map((sourceDetail) => {
                const disabled =
                    !canReextract ||
                    !reportNo ||
                    !sourceDetail.sourceCategoryCode ||
                    !sourceDetail.issueCount;
                return (
                    <button
                        key={sourceDetail.detailId}
                        aria-label={`重提取${sourceDetail.sourceCategoryName || sourceDetail.sourceCategoryCode || "来源明细"}`}
                        disabled={disabled}
                        type="button"
                        onClick={() => onReextract?.(sourceDetail)}
                    >
                        {reextractingDetailId === sourceDetail.detailId ? "重提取中" : "重提取"}
                    </button>
                );
            })}
        </div>
    );
    const module: Record<string, unknown> = {};
    module["QualityReportSourceTable"] = sourceTable;
    return module;
});

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({ danger: confirmDangerMock })
}));

vi.mock("./components/quality-report-source-table", () => qualityReportSourceTableModuleMock);

vi.mock("./quality-report-service", () => ({
    ...qualityReportServiceMock
}));

const queryClients: QueryClient[] = [];

const renderQualityReportPage = () => {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: { gcTime: Infinity, refetchOnWindowFocus: false, retry: false }
        }
    });
    queryClients.push(queryClient);

    render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <QualityReportPage />
            </AntdApp>
        </QueryClientProvider>
    );
};

const openSourceDetailsTab = async () => {
    await userEvent.click(await screen.findByText("来源明细"));
};

describe("QualityReportPage", () => {
    beforeEach(() => {
        confirmDangerMock.mockReset();
        qualityReportServiceMock.getLatestReport.mockClear();
        qualityReportServiceMock.pageReports.mockClear();
        qualityReportServiceMock.getReportDetail.mockClear();
        qualityReportServiceMock.generateReport.mockClear();
        qualityReportServiceMock.reextractLowQualityCategory.mockClear();
        replacePermissions(["knowledge:quality-report:view", "knowledge:quality-report:generate"]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
        cleanup();
        queryClients.splice(0).forEach((queryClient) => queryClient.clear());
    });

    it("renders report generation, summary, history and detail tabs", async () => {
        renderQualityReportPage();

        expect(screen.getByRole("heading", { name: "质量报告" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "生成报告" })).toBeInTheDocument();
        expect(await screen.findAllByText("KQR-1001")).toHaveLength(2);
        expect(screen.getByText("问题清单")).toBeInTheDocument();
        expect(screen.getByText("来源明细")).toBeInTheDocument();
        expect(screen.getByText("人工标注")).toBeInTheDocument();
    }, 30000);

    it("disables reextract without graph edit permission", async () => {
        renderQualityReportPage();

        await openSourceDetailsTab();
        expect(await screen.findByLabelText("重提取神话")).toBeDisabled();
    }, 30000);

    it("disables reextract when source detail has no issue", async () => {
        replacePermissions([
            "knowledge:quality-report:view",
            "knowledge:quality-report:generate",
            "knowledge:graph:edit"
        ]);
        vi.mocked(service.getLatestReport).mockResolvedValueOnce({
            report: {
                reportId: 1001,
                reportNo: "KQR-1001",
                graphVersionId: 71,
                reportStatus: "PUBLISHED"
            },
            sourceDetails: [
                {
                    detailId: 1,
                    sourceCategoryCode: "myth",
                    sourceCategoryName: "神话",
                    issueCount: 0
                }
            ]
        });

        renderQualityReportPage();

        await openSourceDetailsTab();
        expect(await screen.findByLabelText("重提取神话")).toBeDisabled();
    }, 30000);

    it("confirms and creates reextract task for low quality category", async () => {
        const user = userEvent.setup();
        replacePermissions([
            "knowledge:quality-report:view",
            "knowledge:quality-report:generate",
            "knowledge:graph:edit"
        ]);
        renderQualityReportPage();

        await openSourceDetailsTab();
        await user.click(await screen.findByLabelText("重提取神话"));

        expect(confirmDangerMock).toHaveBeenCalledWith(
            expect.objectContaining({
                okText: "重提取",
                title: "确认重提取低质量门类"
            })
        );
        await act(async () => {
            await confirmDangerMock.mock.calls[0][0].onConfirm();
        });

        await waitFor(() => {
            expect(service.reextractLowQualityCategory).toHaveBeenCalledWith(
                expect.objectContaining({
                    reportId: 1001,
                    sourceCategoryCode: "myth",
                    taskType: "GRAPH",
                    replaceUnconfirmedOnly: true,
                    modelId: 1,
                    modelName: "gpt-5.5",
                    inputPayloadJson: '{"triggerSource":"QUALITY_REPORT"}',
                    requestedBy: 1
                })
            );
        });
        expect(await screen.findAllByText("低质量门类重提取任务已创建")).toHaveLength(2);
        expect(screen.getByText("任务号：3001")).toBeInTheDocument();
        expect(screen.getByRole("link", { name: "打开任务台账" })).toHaveAttribute(
            "href",
            "/knowledge/graph-extraction"
        );
    }, 30000);
});
