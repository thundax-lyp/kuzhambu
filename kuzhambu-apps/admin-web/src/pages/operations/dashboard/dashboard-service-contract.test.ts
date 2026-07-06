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

        expect(postJson).toHaveBeenCalledWith("/operations/dashboard/overview", {
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

        expect(postJson).toHaveBeenCalledWith("/operations/health/trend", {
            body: {
                component: "admin-server",
                probeSource: "LOCAL",
                periodStart: "2026-07-01T00:00:00.000+08:00",
                periodEnd: "2026-07-07T23:59:59.000+08:00",
                bucketType: "DAY"
            }
        });
    });
});
