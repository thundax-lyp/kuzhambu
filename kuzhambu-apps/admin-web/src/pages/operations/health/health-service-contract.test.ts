import { beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./health-service";

const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postJson
}));

describe("operations health service contracts", () => {
    beforeEach(() => {
        postJson.mockReset();
    });

    it("maps health page endpoint and body fields", async () => {
        await service.getOperationsHealthPage({
            component: "admin-starter",
            healthStatus: "DEGRADED",
            probeSource: "HTTP",
            probeTarget: "http://127.0.0.1:20010/kuzhambu-admin-api/actuator/health",
            checkedAtStart: "2026-07-01T00:00:00.000+08:00",
            checkedAtEnd: "2026-07-07T23:59:59.000+08:00",
            pageNo: 2,
            pageSize: 20
        });

        expect(postJson).toHaveBeenCalledWith("/operations/health/page", {
            body: {
                component: "admin-starter",
                healthStatus: "DEGRADED",
                probeSource: "HTTP",
                probeTarget: "http://127.0.0.1:20010/kuzhambu-admin-api/actuator/health",
                checkedAtStart: "2026-07-01T00:00:00.000+08:00",
                checkedAtEnd: "2026-07-07T23:59:59.000+08:00",
                pageNo: 2,
                pageSize: 20
            }
        });
    });

    it("maps health alert ledger and action endpoints", async () => {
        await service.getOperationsHealthAlerts({
            latestCheckId: "9101",
            alertStatus: "ACTIVE",
            pageNo: 1,
            pageSize: 10
        });
        await service.confirmOperationsHealthAlert({ alertId: "9201" });
        await service.recoverOperationsHealthAlert({ alertId: "9201" });

        expect(postJson).toHaveBeenNthCalledWith(1, "/operations/health/alerts/page", {
            body: {
                latestCheckId: "9101",
                alertStatus: "ACTIVE",
                pageNo: 1,
                pageSize: 10
            }
        });
        expect(postJson).toHaveBeenNthCalledWith(2, "/operations/health/alerts/confirm", {
            body: { alertId: "9201" }
        });
        expect(postJson).toHaveBeenNthCalledWith(3, "/operations/health/alerts/recover", {
            body: { alertId: "9201" }
        });
    });
});
