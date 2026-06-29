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
        expect(postJson).toHaveBeenLastCalledWith("/operations/backup/execute", {
            body: {}
        });

        await service.pageBackups({
            backupType: "MANUAL",
            backupStatus: "SUCCEEDED",
            requesterUserId: 1001,
            pageNo: 1,
            pageSize: 20
        });
        expect(postJson).toHaveBeenLastCalledWith("/operations/backup/page", {
            body: {
                backupType: "MANUAL",
                backupStatus: "SUCCEEDED",
                requesterUserId: 1001,
                pageNo: 1,
                pageSize: 20
            }
        });

        await service.getBackupDetail({ backupId: 9001 });
        expect(postJson).toHaveBeenLastCalledWith("/operations/backup/detail", {
            body: {
                backupId: 9001
            }
        });
    });

    it("maps restore endpoints and request bodies", async () => {
        await service.recoverBackup({ backupId: 9001 });
        expect(postJson).toHaveBeenLastCalledWith("/operations/restore/execute", {
            body: {
                backupId: 9001
            }
        });

        await service.pageRestores({
            backupId: 9001,
            restoreStatus: "SUCCEEDED",
            requesterUserId: 1001,
            pageNo: 1,
            pageSize: 20
        });
        expect(postJson).toHaveBeenLastCalledWith("/operations/restore/page", {
            body: {
                backupId: 9001,
                restoreStatus: "SUCCEEDED",
                requesterUserId: 1001,
                pageNo: 1,
                pageSize: 20
            }
        });

        await service.getRestoreDetail({ restoreId: 9101 });
        expect(postJson).toHaveBeenLastCalledWith("/operations/restore/detail", {
            body: {
                restoreId: 9101
            }
        });
    });
});
