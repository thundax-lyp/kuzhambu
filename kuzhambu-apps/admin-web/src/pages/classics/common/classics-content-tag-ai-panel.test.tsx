import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { KuzhambuSyncTaskModalProps, KuzhambuSyncTaskModalState } from "@/components";
import { ClassicsContentTagAiPanel } from "./classics-content-tag-ai-panel";

const tagCandidate = {
    candidateId: "872517961657614336",
    candidateIdText: "872517961657614321",
    capability: "CLASSICS_TAG_EXTRACT",
    contentType: "SANCAI_ENTRY",
    contentId: "8",
    resultFormat: "STRUCTURED",
    resultPayload: '{"tags":["旧标签"]}',
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
            result: tagCandidate,
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

        if (path.endsWith("/classics/content/tags/list")) {
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
                ...tagCandidate,
                status: "REJECTED"
            });
        }

        return apiResponse(true);
    });
};

const renderPanel = () => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } }
    });

    render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <ClassicsContentTagAiPanel
                    canApplyCandidate
                    canRejectCandidate
                    canViewCandidate
                    contentId="8"
                    contentType="SANCAI_ENTRY"
                />
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("ClassicsContentTagAiPanel", () => {
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

    it("applies edited tag candidates", async () => {
        const user = userEvent.setup();
        renderPanel();

        await user.click(screen.getByTestId("classics-common-content-tag-ai-button"));
        expect(await screen.findByLabelText("候选标签 1")).toHaveValue("旧标签");
        await user.clear(screen.getByLabelText("候选标签 1"));
        await user.type(screen.getByLabelText("候选标签 1"), "修正标签");
        await user.click(screen.getByTestId("classics-content-tag-ai-generated-add-button"));
        await user.type(screen.getByLabelText("候选标签 2"), "新增标签");
        await waitFor(() => {
            expect(screen.getByTestId("classics-content-tag-ai-append-button")).not.toBeDisabled();
        });

        await user.click(screen.getByTestId("classics-content-tag-ai-append-button"));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    method: "POST",
                    path: "/classics/content/ai-candidates/change",
                    body: expect.objectContaining({
                        candidateId: "872517961657614321",
                        capability: "CLASSICS_TAG_EXTRACT",
                        contentId: "8",
                        contentType: "SANCAI_ENTRY",
                        resultPayload: JSON.stringify({
                            tags: ["修正标签", "新增标签"]
                        }),
                        tagApplyMode: "APPEND"
                    })
                })
            );
        });
    });

    it("rejects tag candidates", async () => {
        const user = userEvent.setup();
        renderPanel();

        await user.click(screen.getByTestId("classics-common-content-tag-ai-button"));
        await waitFor(() => {
            expect(screen.getByTestId("classics-content-tag-ai-reject-button")).not.toBeDisabled();
        });
        await user.click(screen.getByTestId("classics-content-tag-ai-reject-button"));

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
});
