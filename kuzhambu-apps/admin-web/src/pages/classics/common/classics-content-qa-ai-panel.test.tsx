import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { KuzhambuSyncTaskModalProps, KuzhambuSyncTaskModalState } from "@/components";
import { selectLatestQaCandidate } from "./classics-content-ai-candidate-selectors";
import { ClassicsContentQaAiPanel } from "./classics-content-qa-ai-panel";

const qaCandidate = {
    candidateId: "872517961657614336",
    candidateIdText: "872517961657614321",
    capability: "CLASSICS_QA",
    contentType: "SANCAI_ENTRY",
    contentId: "8",
    resultFormat: "STRUCTURED",
    resultPayload: '{"qaPairs":[{"question":"问","answer":"答"}]}',
    status: "PENDING"
};

vi.mock("@/components", async (importOriginal) => {
    const actual = await importOriginal<typeof import("@/components")>();
    const mocks = { ...actual };
    const MockKuzhambuSyncTaskModal = <TTask, TResult>({
        open,
        renderBody,
        renderFooterActions
    }: KuzhambuSyncTaskModalProps<TTask, TResult>) => {
        if (!open) {
            return <></>;
        }

        const state = {
            canApply: true,
            canCreate: true,
            creating: false,
            isBusy: false,
            phase: "result_ready",
            refetchResult: vi.fn(),
            refetchTask: vi.fn(),
            result: qaCandidate,
            resultError: null,
            resultLoading: false,
            task: null,
            taskError: null,
            taskLoading: false,
            tracking: false
        } as unknown as KuzhambuSyncTaskModalState<TTask, TResult>;

        return (
            <div>
                {renderBody(state)}
                {renderFooterActions?.(state)}
            </div>
        );
    };

    mocks.KuzhambuSyncTaskModal = MockKuzhambuSyncTaskModal;
    return mocks;
});

interface CapturedCall {
    body: unknown;
    method: string;
    path: string;
}

const capturedCalls: CapturedCall[] = [];

const apiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data }), {
            headers: { "Content-Type": "application/json" },
            status: 200
        })
    );

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

const installFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
        const path = readFetchUrl(input).replace("/kuzhambu-admin-api/api", "");
        const method = init?.method ?? "GET";
        const body = readFetchBody(init?.body);
        capturedCalls.push({ body, method, path });

        if (path.endsWith("/classics/content/qa-pairs/list")) {
            return apiResponse([]);
        }

        if (path.endsWith("/classics/content/ai-candidates/change")) {
            return apiResponse({
                contentType: "SANCAI_ENTRY",
                contentId: "8",
                versionId: "9001",
                versionNo: 2
            });
        }

        if (path.endsWith("/ai/invocation/candidate/reject")) {
            return apiResponse({
                ...qaCandidate,
                status: "REJECTED"
            });
        }

        return apiResponse(true);
    });
};

describe("ClassicsContentQaAiPanel", () => {
    beforeEach(() => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.accessTokenExpireAt",
            String(Date.now() + 3600 * 1000)
        );
        capturedCalls.length = 0;
        installFetchMock();
    });

    afterEach(() => {
        vi.restoreAllMocks();
        localStorage.clear();
    });

    it("applies qa candidate with stable candidate id and original capability", async () => {
        const queryClient = new QueryClient({
            defaultOptions: { queries: { retry: false } }
        });
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <ClassicsContentQaAiPanel
                        canApplyCandidate
                        canRejectCandidate
                        canViewCandidate
                        contentId="8"
                        contentType="SANCAI_ENTRY"
                    />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(screen.getByTestId("classics-common-content-qa-ai-button"));
        expect(await screen.findByLabelText("候选问题 1")).toHaveValue("问");
        expect(screen.getByLabelText("候选答案 1")).toHaveValue("答");
        await user.clear(screen.getByLabelText("候选问题 1"));
        await user.type(screen.getByLabelText("候选问题 1"), "修改后的问题");
        await user.click(screen.getByTestId("classics-content-qa-ai-generated-add-button"));
        await user.type(screen.getByLabelText("候选问题 2"), "新增问题");
        await user.type(screen.getByLabelText("候选答案 2"), "新增答案");
        await waitFor(() => {
            expect(screen.getByTestId("classics-content-qa-ai-append-button")).not.toBeDisabled();
        });

        await user.click(screen.getByTestId("classics-content-qa-ai-append-button"));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    method: "POST",
                    path: "/classics/content/ai-candidates/change",
                    body: expect.objectContaining({
                        candidateId: "872517961657614321",
                        capability: "CLASSICS_QA",
                        contentId: "8",
                        contentType: "SANCAI_ENTRY",
                        resultPayload: JSON.stringify({
                            qaPairs: [
                                { question: "修改后的问题", answer: "答" },
                                { question: "新增问题", answer: "新增答案" }
                            ]
                        })
                    })
                })
            );
        });
    });

    it("rejects qa candidates", async () => {
        const queryClient = new QueryClient({
            defaultOptions: { queries: { retry: false } }
        });
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <ClassicsContentQaAiPanel
                        canApplyCandidate
                        canRejectCandidate
                        canViewCandidate
                        contentId="8"
                        contentType="SANCAI_ENTRY"
                    />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(screen.getByTestId("classics-common-content-qa-ai-button"));
        await waitFor(() => {
            expect(screen.getByTestId("classics-content-qa-ai-reject-button")).not.toBeDisabled();
        });
        await user.click(screen.getByTestId("classics-content-qa-ai-reject-button"));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    method: "POST",
                    path: "/ai/invocation/candidate/reject",
                    body: {
                        candidateId: "872517961657614321",
                        errorType: "USER_REJECTED",
                        errorMessage: "用户已拒绝该 AI 候选"
                    }
                })
            );
        });
    });

    it("filters tracked qa candidates by exact stable id only", () => {
        const selected = selectLatestQaCandidate(
            [
                {
                    ...qaCandidate,
                    candidateId: "872517961657614336",
                    candidateIdText: "872517961657614321",
                    requestedAt: "2026-08-06T09:00:00Z"
                },
                {
                    ...qaCandidate,
                    candidateId: "872517961657614336",
                    candidateIdText: "872517961657614322",
                    requestedAt: "2026-08-06T09:01:00Z"
                }
            ],
            "872517961657614321"
        );

        expect(selected?.candidateIdText).toBe("872517961657614321");
    });
});
