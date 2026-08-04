import { beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./report-service";

const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    ADMIN_API_BASE_URL: "/kuzhambu-admin-api/api",
    postJson
}));

describe("operations reports service contracts", () => {
    beforeEach(() => {
        postJson.mockReset();
    });

    it("maps report generate, page, detail and download endpoints", async () => {
        await service.generateReport({
            reportType: "WEEKLY",
            format: "PDF",
            periodStart: "2026-07-01T00:00:00.000+08:00",
            periodEnd: "2026-07-07T23:59:59.000+08:00"
        });
        expect(postJson).toHaveBeenLastCalledWith("/operations/report/generate", {
            body: {
                reportType: "WEEKLY",
                format: "PDF",
                periodStart: "2026-07-01T00:00:00.000+08:00",
                periodEnd: "2026-07-07T23:59:59.000+08:00"
            }
        });

        await service.pageReports({
            reportType: "MONTHLY",
            format: "HTML",
            reportStatus: "SUCCEEDED",
            requesterUserId: "1001",
            periodStart: "2026-07-01T00:00:00.000+08:00",
            periodEnd: "2026-07-31T23:59:59.000+08:00",
            pageNo: 2,
            pageSize: 10
        });
        expect(postJson).toHaveBeenLastCalledWith("/operations/report/page", {
            body: {
                reportType: "MONTHLY",
                format: "HTML",
                reportStatus: "SUCCEEDED",
                requesterUserId: "1001",
                periodStart: "2026-07-01T00:00:00.000+08:00",
                periodEnd: "2026-07-31T23:59:59.000+08:00",
                pageNo: 2,
                pageSize: 10
            }
        });

        await service.getReportDetail({ reportId: "9001" });
        expect(postJson).toHaveBeenLastCalledWith("/operations/report/detail", {
            body: {
                reportId: "9001"
            }
        });

        expect(service.toReportDownloadUrl("9001")).toBe(
            "/kuzhambu-admin-api/api/operations/report/9001/content?download=true"
        );
    });
});
