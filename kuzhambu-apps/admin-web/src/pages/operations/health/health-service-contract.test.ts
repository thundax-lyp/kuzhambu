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
});
