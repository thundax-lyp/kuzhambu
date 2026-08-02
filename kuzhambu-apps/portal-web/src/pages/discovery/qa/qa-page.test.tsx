import { createRoot } from "react-dom/client";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { DiscoveryQaPage } from "./qa-page";

const mocks = vi.hoisted(() => ({
    createQaChatCompletionStream: vi.fn(),
    deleteQaSession: vi.fn(),
    exportQaSession: vi.fn(),
    getQaSession: vi.fn(),
    openQaSession: vi.fn(),
    pageQaSessions: vi.fn(),
    postEventStream: vi.fn(),
    postJson: vi.fn()
}));

vi.mock("@/api/http", () => ({
    postEventStream: mocks.postEventStream,
    postJson: mocks.postJson
}));

vi.mock("./qa-service", () => mocks);

const renderPage = (initialEntry = "/discovery/qa") => {
    const container = document.createElement("div");
    document.body.appendChild(container);

    const queryClient = new QueryClient({
        defaultOptions: {
            mutations: {
                retry: false
            },
            queries: {
                retry: false
            }
        }
    });
    const root = createRoot(container);

    act(() => {
        root.render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter initialEntries={[initialEntry]}>
                    <DiscoveryQaPage />
                </MemoryRouter>
            </QueryClientProvider>
        );
    });

    return { container, root };
};

const setTextareaValue = (container: HTMLElement, name: string, value: string) => {
    const textarea = container.querySelector(
        `textarea[name="${name}"]`
    ) as HTMLTextAreaElement | null;
    expect(textarea).not.toBeNull();

    act(() => {
        if (!textarea) {
            return;
        }

        const setter = Object.getOwnPropertyDescriptor(
            window.HTMLTextAreaElement.prototype,
            "value"
        )?.set;
        setter?.call(textarea, value);
        textarea.dispatchEvent(new Event("input", { bubbles: true }));
    });
};

const pressTextareaKey = async (
    container: HTMLElement,
    name: string,
    key: string,
    shiftKey = false
) => {
    const textarea = container.querySelector(
        `textarea[name="${name}"]`
    ) as HTMLTextAreaElement | null;
    expect(textarea).not.toBeNull();

    await act(async () => {
        textarea?.dispatchEvent(new KeyboardEvent("keydown", { bubbles: true, key, shiftKey }));
        await new Promise((resolve) => setTimeout(resolve, 0));
    });
};

describe("DiscoveryQaPage", () => {
    afterEach(() => {
        mocks.createQaChatCompletionStream.mockReset();
        mocks.deleteQaSession.mockReset();
        mocks.exportQaSession.mockReset();
        mocks.getQaSession.mockReset();
        mocks.openQaSession.mockReset();
        mocks.pageQaSessions.mockReset();
        mocks.postEventStream.mockReset();
        mocks.postJson.mockReset();
        localStorage.clear();
        document.body.innerHTML = "";
    });

    it("auto opens session on first question and then sends chat/completions", async () => {
        mocks.openQaSession.mockResolvedValueOnce({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            lastMessageAt: null,
            openedAt: 1699999999000,
            scope: "PORTAL",
            id: 2001,
            status: "OPEN",
            title: "知识中心问答"
        });
        mocks.createQaChatCompletionStream.mockResolvedValueOnce({
            answerStatus: "SUCCEEDED",
            model: "kuzhambu-qa",
            choices: [
                {
                    index: 0,
                    message: {
                        content: "礼器常见于典章与礼仪条目。",
                        role: "assistant"
                    },
                    finishReason: "stop"
                }
            ],
            id: "chat-2001",
            sources: [
                {
                    contentId: 1001,
                    contentType: "SANCAI_ENTRY",
                    knowledgeBase: "kuzhambu-qa",
                    score: 0.91,
                    sourceId: "SANCAI_ENTRY:1001",
                    sourceRank: 1,
                    sourceStatus: "AVAILABLE",
                    sourcePath: "/classics/sancai?entryId=1001",
                    snippet: "礼器在章节中常用于秩序相关记载。",
                    titleSnapshot: null
                }
            ],
            sessionId: "2001"
        });

        const { container, root } = renderPage();

        expect(container.querySelector('textarea[name="question"]')).not.toBeNull();
        expect(container.textContent).not.toContain("会话列表");
        expect(container.textContent).not.toContain("删除会话");
        expect(container.textContent).not.toContain("导出 CSV");

        setTextareaValue(container, "question", "礼器是什么？");

        const submitButton = container.querySelector(
            'button[type="submit"]'
        ) as HTMLButtonElement | null;
        expect(submitButton).not.toBeNull();

        await act(async () => {
            submitButton?.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(mocks.openQaSession.mock.calls[0]?.[0]).toEqual({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            ownerUserId: 1001,
            requestId: null,
            scope: "PORTAL",
            title: "知识中心问答",
            traceId: null
        });
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
            onDelta: expect.any(Function),
            request: {
                model: "kuzhambu-qa",
                messages: [{ content: "礼器是什么？", role: "user" }],
                metadata: { contextContentId: null, contextContentType: null, sessionId: "2001" },
                sessionId: "2001",
                stream: true
            }
        });
        expect(container.textContent).toContain("礼器常见于典章与礼仪条目。");
        expect(container.textContent).toContain("SANCAI_ENTRY:1001");
        expect(container.querySelectorAll(".portal-qa-avatar")).toHaveLength(2);
        expect(container.querySelector('a[href="/classics/sancai?entryId=1001"]')).not.toBeNull();

        act(() => {
            root.unmount();
        });
    });

    it("sends question with Enter and keeps Shift Enter for multiline input", async () => {
        mocks.openQaSession.mockResolvedValueOnce({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            lastMessageAt: null,
            openedAt: 1699999999000,
            scope: "PORTAL",
            id: "2101",
            status: "OPEN",
            title: "知识中心问答"
        });
        mocks.createQaChatCompletionStream.mockResolvedValueOnce({
            answerStatus: "SUCCEEDED",
            choices: [
                {
                    finishReason: "stop",
                    index: 0,
                    message: {
                        content: "Enter 已发送。",
                        role: "assistant"
                    }
                }
            ],
            sessionId: "2101"
        });

        const { container, root } = renderPage();

        setTextareaValue(container, "question", "第一行");
        await pressTextareaKey(container, "question", "Enter", true);
        expect(mocks.openQaSession).not.toHaveBeenCalled();
        expect(mocks.createQaChatCompletionStream).not.toHaveBeenCalled();

        await pressTextareaKey(container, "question", "Enter");

        expect(mocks.openQaSession).toHaveBeenCalledTimes(1);
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
            request: {
                messages: [{ content: "第一行", role: "user" }],
                sessionId: "2101"
            }
        });
        expect(container.textContent).toContain("Enter 已发送。");

        act(() => {
            root.unmount();
        });
    });

    it("preserves portal qa stream error event message", async () => {
        const qaService = await vi.importActual<typeof import("./qa-service")>("./qa-service");
        const onError = vi.fn();
        mocks.postEventStream.mockImplementationOnce(async (_url, options) => {
            options.onChunk('event:error\ndata:{"message":"FastGPT appId 未配置"}\n\n');
        });

        await expect(
            qaService.createQaChatCompletionStream({
                onError,
                request: {
                    messages: [{ content: "礼器是什么？", role: "user" }],
                    metadata: { sessionId: "2001" },
                    model: "kuzhambu-qa",
                    sessionId: "2001",
                    stream: true
                }
            })
        ).rejects.toThrow("FastGPT appId 未配置");

        expect(onError).toHaveBeenCalledWith("FastGPT appId 未配置");
    });
});
