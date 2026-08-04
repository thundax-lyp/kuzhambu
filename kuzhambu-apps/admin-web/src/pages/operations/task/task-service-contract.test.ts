import { beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./task-service";

const postJson = vi.hoisted(() => vi.fn());

vi.mock("@/api/http", () => ({
    postJson
}));

describe("operations tasks service contracts", () => {
    beforeEach(() => {
        postJson.mockReset();
    });

    it("maps task page and detail endpoints", async () => {
        await service.pageTasks({
            sourceDomain: "operations",
            taskType: "cleanup",
            taskStatus: "RUNNING",
            pageNo: 2,
            pageSize: 10
        });
        expect(postJson).toHaveBeenLastCalledWith("/operations/task/page", {
            body: {
                sourceDomain: "operations",
                taskType: "cleanup",
                taskStatus: "RUNNING",
                pageNo: 2,
                pageSize: 10
            }
        });

        await service.getTaskDetail({ snapshotId: "1001" });
        expect(postJson).toHaveBeenCalledWith("/operations/task/detail", {
            body: {
                snapshotId: "1001"
            }
        });
    });
});
