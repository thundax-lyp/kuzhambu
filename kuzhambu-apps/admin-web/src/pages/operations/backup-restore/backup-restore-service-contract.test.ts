import { beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./backup-restore-service";

const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postJson
}));

describe("operations backup restore service contracts", () => {
    beforeEach(() => {
        postJson.mockReset();
    });

    it("maps backup endpoints and request bodies", async () => {
        await service.createManualBackup();
        expect(postJson).toHaveBeenLastCalledWith("/operations/backup/create", {
            body: {}
        });

        await service.pageBackups({
            backupType: "MANUAL",
            backupStatus: "SUCCEEDED",
            requesterUserId: "1001",
            pageNo: 1,
            pageSize: 20
        });
        expect(postJson).toHaveBeenLastCalledWith("/operations/backup/page", {
            body: {
                backupType: "MANUAL",
                backupStatus: "SUCCEEDED",
                requesterUserId: "1001",
                pageNo: 1,
                pageSize: 20
            }
        });

        await service.getBackupDetail({ backupId: "9001" });
        expect(postJson).toHaveBeenLastCalledWith("/operations/backup/get", {
            body: {
                backupId: "9001"
            }
        });
    });

    it("maps restore endpoints and request bodies", async () => {
        await service.recoverBackup({ backupId: "9001", restoreMode: "DRILL" });
        expect(postJson).toHaveBeenLastCalledWith("/operations/restore/create", {
            body: {
                backupId: "9001",
                restoreMode: "DRILL"
            }
        });

        await service.pageRestores({
            backupId: "9001",
            restoreMode: "DRILL",
            restoreStatus: "SUCCEEDED",
            requesterUserId: "1001",
            pageNo: 1,
            pageSize: 20
        });
        expect(postJson).toHaveBeenLastCalledWith("/operations/restore/page", {
            body: {
                backupId: "9001",
                restoreMode: "DRILL",
                restoreStatus: "SUCCEEDED",
                requesterUserId: "1001",
                pageNo: 1,
                pageSize: 20
            }
        });

        await service.getRestoreDetail({ restoreId: "9101" });
        expect(postJson).toHaveBeenLastCalledWith("/operations/restore/get", {
            body: {
                restoreId: "9101"
            }
        });
    });

    it("keeps restore response fields available to callers", async () => {
        postJson.mockResolvedValueOnce({
            restoreId: "9101",
            backupId: "9001",
            preRestoreBackupId: "9201",
            restoreMode: "DRILL",
            restoreStatus: "SUCCEEDED",
            writeBlockEnabled: true,
            writeBlockStartedAt: "2026-07-06T02:00:00.000Z",
            writeBlockReleasedAt: "2026-07-06T02:03:00.000Z"
        });

        const response = await service.recoverBackup({ backupId: "9001", restoreMode: "DRILL" });

        expect(response.restoreMode).toBe("DRILL");
        expect(response.writeBlockStartedAt).toBe("2026-07-06T02:00:00.000Z");
        expect(response.writeBlockReleasedAt).toBe("2026-07-06T02:03:00.000Z");
    });
});
