import { beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./cleanup-service";

const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postJson
}));

describe("operations cleanup service contracts", () => {
    beforeEach(() => {
        postJson.mockReset();
    });

    it("maps cleanup execute endpoint", async () => {
        await service.requestCleanup({ cleanupType: "EXPIRED_BACKUP" });
        expect(postJson).toHaveBeenLastCalledWith("/operations/cleanup/execute", {
            body: {
                cleanupType: "EXPIRED_BACKUP"
            }
        });
    });

    it("maps cleanup page and detail endpoints", async () => {
        await service.pageCleanups({
            cleanupType: "EXPIRED_BACKUP",
            cleanupStatus: "SUCCEEDED",
            requesterUserId: 1001,
            pageNo: 1,
            pageSize: 20
        });
        expect(postJson).toHaveBeenLastCalledWith("/operations/cleanup/page", {
            body: {
                cleanupType: "EXPIRED_BACKUP",
                cleanupStatus: "SUCCEEDED",
                requesterUserId: 1001,
                pageNo: 1,
                pageSize: 20
            }
        });

        postJson.mockResolvedValueOnce({
            cleanupId: 901,
            items: [
                {
                    cleanupItemId: 9201,
                    targetType: "share",
                    targetId: 201,
                    itemStatus: "FAILED",
                    failureReason: "TARGET_NOT_FOUND",
                    processedAt: "2026-07-06T10:00:00Z"
                }
            ]
        });
        const detail = await service.getCleanupDetail({ cleanupId: 901 });
        expect(postJson).toHaveBeenLastCalledWith("/operations/cleanup/detail", {
            body: {
                cleanupId: 901
            }
        });
        expect(detail.items?.[0]?.targetType).toBe("share");
        expect(detail.items?.[0]?.failureReason).toBe("TARGET_NOT_FOUND");
    });
});
