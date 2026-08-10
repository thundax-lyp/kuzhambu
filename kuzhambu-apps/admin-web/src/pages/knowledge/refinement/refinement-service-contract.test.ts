import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as service from "./refinement-service";

interface CapturedCall {
    body: unknown;
    method: string | undefined;
    path: string;
}

const API_PREFIX = "http://localhost:20010";
const DEV_PROXY_PREFIX = "/kuzhambu-admin-api/api";
const capturedCalls: CapturedCall[] = [];

const readFetchUrl = (input: RequestInfo | URL) => {
    if (typeof input === "string") {
        return input;
    }
    if (input instanceof URL) {
        return input.href;
    }
    return input.url;
};

const readFetchBody = (body: BodyInit | null | undefined) => {
    if (!body) {
        return undefined;
    }
    return JSON.parse(String(body));
};

const installFetchRecorder = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
        const url = readFetchUrl(input);
        capturedCalls.push({
            body: readFetchBody(init?.body),
            method: init?.method,
            path: url.replace(API_PREFIX, "").replace(DEV_PROXY_PREFIX, "")
        });
        return new Response(
            JSON.stringify({
                code: "COMMON-00000",
                message: "success",
                data: {
                    refinementTaskId: "31",
                    status: "DRAFT"
                }
            }),
            { headers: { "Content-Type": "application/json" }, status: 200 }
        );
    });
};

const expectLastCall = (method: string, path: string, body: unknown) => {
    expect(capturedCalls.at(-1)).toEqual({ body, method, path });
};

describe("knowledge refinement service request contracts", () => {
    beforeEach(() => {
        capturedCalls.length = 0;
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

    it("sends task page, open, detail and apply requests", async () => {
        await service.pageTasks({
            pageNo: 1,
            pageSize: 20,
            taskType: "GRAPH",
            sourceCategoryCode: "myth"
        });
        expectLastCall("POST", "/knowledge/refinement/task/page", {
            pageNo: 1,
            pageSize: 20,
            taskType: "GRAPH",
            sourceCategoryCode: "myth"
        });

        await service.getTaskDraft({ graphVersionId: "71", openedBy: "1" });
        expectLastCall("POST", "/knowledge/refinement/task/create", {
            graphVersionId: "71",
            openedBy: "1"
        });

        await service.getTaskDetail({ refinementTaskId: "31" });
        expectLastCall("POST", "/knowledge/refinement/task/get", {
            refinementTaskId: "31"
        });

        await service.applyTask({ refinementTaskId: "31", appliedBy: "1" });
        expectLastCall("POST", "/knowledge/refinement/task/apply", {
            refinementTaskId: "31",
            appliedBy: "1"
        });
    });

    it("sends entity, relation and quality requests", async () => {
        await service.addEntity({
            refinementTaskId: "31",
            name: "黄帝",
            entityType: "PERSON",
            operatorId: "1"
        });
        expectLastCall("POST", "/knowledge/refinement/entity/add", {
            refinementTaskId: "31",
            name: "黄帝",
            entityType: "PERSON",
            operatorId: "1"
        });

        await service.updateRelation({
            refinementTaskId: "31",
            relationKey: "person:huangdi->person:fuxi:ancestor",
            relationType: "ANCESTOR",
            operatorId: "1"
        });
        expectLastCall("POST", "/knowledge/refinement/relation/update", {
            refinementTaskId: "31",
            relationKey: "person:huangdi->person:fuxi:ancestor",
            relationType: "ANCESTOR",
            operatorId: "1"
        });

        await service.getQualitySummary({ refinementTaskId: "31" });
        expectLastCall("POST", "/knowledge/refinement/quality/get", {
            refinementTaskId: "31"
        });

        await service.pageAnnotations({
            pageNo: 1,
            pageSize: 20,
            refinementTaskId: "31",
            objectType: "ENTITY"
        });
        expectLastCall("POST", "/knowledge/refinement/annotation/page", {
            pageNo: 1,
            pageSize: 20,
            refinementTaskId: "31",
            objectType: "ENTITY"
        });
    });
});
