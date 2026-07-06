import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
import { QualityReportPage } from "./quality-report-page";

vi.mock("./quality-report-service", () => ({
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
                graphVersionId: 71,
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
    getReportDetail: vi.fn(async () => null),
    generateReport: vi.fn(async () => null)
}));

describe("QualityReportPage", () => {
    beforeEach(() => {
        replacePermissions(["knowledge:quality-report:view", "knowledge:quality-report:generate"]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
        cleanup();
    });

    it("renders report generation, summary, history and detail tabs", async () => {
        const queryClient = new QueryClient({
            defaultOptions: {
                queries: { gcTime: Infinity, refetchOnWindowFocus: false, retry: false }
            }
        });

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <QualityReportPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(screen.getByRole("heading", { name: "质量报告" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "生成报告" })).toBeInTheDocument();
        expect(await screen.findAllByText("KQR-1001")).toHaveLength(2);
        expect(screen.getByText("问题清单")).toBeInTheDocument();
        expect(screen.getByText("来源明细")).toBeInTheDocument();
        expect(screen.getByText("人工标注")).toBeInTheDocument();
    }, 30000);
});
