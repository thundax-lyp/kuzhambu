import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as departmentService from "@/pages/system/department/department-service";

interface CapturedCall {
    path: string;
    body: unknown;
}

const API_PREFIX = "http://localhost:20010";
const DEV_PROXY_PREFIX = "/kuzhambu-admin-api/api";

const capturedRequests: CapturedCall[] = [];

const readFetchUrl = (input: RequestInfo | URL) => {
    if (typeof input === "string") {
        return input;
    }
    if (input instanceof URL) {
        return input.href;
    }
    return input.url;
};

const installFetchRecorder = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
        const url = readFetchUrl(input);
        const path = url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, "");
        capturedRequests.push({
            path,
            body: init?.body ? JSON.parse(String(init.body)) : undefined
        });

        return new Response(
            JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: true
            }),
            {
                headers: {
                    "Content-Type": "application/json"
                },
                status: 200
            }
        );
    });
};

const expectLastRequest = (path: string, body: unknown) => {
    expect(capturedRequests.at(-1)).toEqual({
        path,
        body
    });
};

describe("department service request contracts", () => {
    beforeEach(() => {
        capturedRequests.length = 0;
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.accessTokenExpireAt",
            String(Date.now() + 3600 * 1000)
        );
        installFetchRecorder();
    });

    afterEach(() => {
        vi.restoreAllMocks();
        localStorage.clear();
    });

    it("sends department write requests with Department request fields", async () => {
        const saveRequest: departmentService.DepartmentSaveCommand = {
            id: "dept-1",
            parentId: "dept-root",
            name: "技术中心",
            shortName: "技术",
            remarks: "研发组织"
        };

        await departmentService.addDepartment(saveRequest);
        expectLastRequest("/sys/department/create", saveRequest);

        await departmentService.changeDepartmentInfo(saveRequest);
        expectLastRequest("/sys/department/update", saveRequest);

        await departmentService.moveDepartment({
            fromNodeId: "dept-1",
            toNodeId: "dept-2",
            type: "insideLast"
        });
        expectLastRequest("/sys/department/move", {
            fromNodeId: "dept-1",
            toNodeId: "dept-2",
            type: "insideLast"
        });

        await departmentService.removeDepartments(["dept-1"]);
        expectLastRequest("/sys/department/delete", [{ id: "dept-1" }]);
    });
});
