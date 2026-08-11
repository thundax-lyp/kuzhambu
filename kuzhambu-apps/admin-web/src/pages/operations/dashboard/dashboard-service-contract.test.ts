import { beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./dashboard-service";

const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postJson
}));

describe("operations dashboard service contracts", () => {
    beforeEach(() => {
        postJson.mockReset();
    });

    it("maps dashboard overview endpoint and body fields", async () => {
        await service.getDashboardOverview({
            periodType: "CUSTOM",
            periodStart: "2026-07-01T00:00:00.000+08:00",
            periodEnd: "2026-07-07T23:59:59.000+08:00"
        });

        expect(postJson).toHaveBeenCalledWith("/operations/dashboard/get", {
            body: {
                periodType: "CUSTOM",
                periodStart: "2026-07-01T00:00:00.000+08:00",
                periodEnd: "2026-07-07T23:59:59.000+08:00"
            }
        });
    });

    it("maps health trend endpoint and body fields", async () => {
        await service.getHealthTrend({
            component: "admin-server",
            probeSource: "LOCAL",
            periodStart: "2026-07-01T00:00:00.000+08:00",
            periodEnd: "2026-07-07T23:59:59.000+08:00",
            bucketType: "DAY"
        });

        expect(postJson).toHaveBeenCalledWith("/operations/health/get", {
            body: {
                component: "admin-server",
                probeSource: "LOCAL",
                periodStart: "2026-07-01T00:00:00.000+08:00",
                periodEnd: "2026-07-07T23:59:59.000+08:00",
                bucketType: "DAY"
            }
        });
    });

    it("maps health alert page endpoint and body fields", async () => {
        await service.getHealthAlerts({
            component: "database",
            alertLevel: "CRITICAL",
            alertStatus: "ACTIVE",
            sourceRefType: "HEALTH",
            sourceRefId: "9001",
            pageNo: 1,
            pageSize: 20
        });

        expect(postJson).toHaveBeenCalledWith("/operations/health/alerts/page", {
            body: {
                component: "database",
                alertLevel: "CRITICAL",
                alertStatus: "ACTIVE",
                sourceRefType: "HEALTH",
                sourceRefId: "9001",
                pageNo: 1,
                pageSize: 20
            }
        });
    });

    it("maps health alert action endpoints and body fields", async () => {
        await service.confirmHealthAlert({ alertId: "9201" });
        expect(postJson).toHaveBeenCalledWith("/operations/health/alerts/confirm", {
            body: {
                alertId: "9201"
            }
        });

        await service.recoverHealthAlert({ alertId: "9201" });
        expect(postJson).toHaveBeenCalledWith("/operations/health/alerts/recover", {
            body: {
                alertId: "9201"
            }
        });
    });
});
