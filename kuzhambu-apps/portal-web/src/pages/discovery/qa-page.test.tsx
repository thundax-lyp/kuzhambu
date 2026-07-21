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

const findButtonByText = (container: HTMLElement, text: string) => {
    const button = Array.from(container.querySelectorAll("button")).find(
        (element) => element.textContent === text
    );

    expect(button).toBeDefined();
    return button as HTMLButtonElement;
};

const flushAsyncWork = async () => {
    await act(async () => {
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
        document.body.innerHTML = "";
    });

    it("auto opens session on first question and then sends chat/completions", async () => {
        mocks.pageQaSessions.mockResolvedValue({
            items: []
        });
        mocks.openQaSession.mockResolvedValueOnce({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            lastMessageAt: null,
            openedAt: 1699999999000,
            scope: "PORTAL",
            sessionId: "2001",
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
                    sourcePath: "/shares/1001",
                    snippet: "礼器在章节中常用于秩序相关记载。",
                    titleSnapshot: "礼器条目"
                }
            ],
            sessionId: "2001"
        });

        const { container, root } = renderPage();

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
        expect(container.textContent).toContain("礼器条目");
        expect(container.querySelector('a[href="/shares/1001"]')).not.toBeNull();

        act(() => {
            root.unmount();
        });
    });

    it("restores Wangqi single document context from url", async () => {
        mocks.pageQaSessions.mockResolvedValue({
            items: []
        });

        const { container, root } = renderPage(
            "/discovery/qa?contextContentType=WANGQI_DOCUMENT&contextContentId=3001&contextMode=SINGLE_DOCUMENT&title=%E7%8E%8B%E5%9C%BB%E5%AE%98%E5%88%B6"
        );
        await flushAsyncWork();

        expect(container.textContent).toContain("当前围绕王圻文档追问");
        expect(container.textContent).toContain("WANGQI_DOCUMENT #3001");

        const titleInput = container.querySelector(
            'input[name="sessionTitle"]'
        ) as HTMLInputElement | null;
        const contextModeInput = container.querySelector(
            'input[name="contextMode"]'
        ) as HTMLInputElement | null;
        const contextTypeInput = container.querySelector(
            'input[name="contextContentType"]'
        ) as HTMLInputElement | null;
        const contextIdInput = container.querySelector(
            'input[name="contextContentId"]'
        ) as HTMLInputElement | null;

        expect(titleInput?.value).toBe("王圻官制");
        expect(contextModeInput?.value).toBe("SINGLE_DOCUMENT");
        expect(contextTypeInput?.value).toBe("WANGQI_DOCUMENT");
        expect(contextIdInput?.value).toBe("3001");
        expect(contextModeInput?.disabled).toBe(true);
        expect(contextTypeInput?.disabled).toBe(true);
        expect(contextIdInput?.disabled).toBe(true);

        act(() => {
            root.unmount();
        });
    });

    it("sends Wangqi single document context in open session and chat metadata", async () => {
        mocks.pageQaSessions.mockResolvedValue({
            items: []
        });
        mocks.openQaSession.mockResolvedValueOnce({
            contextContentId: 3001,
            contextContentType: "WANGQI_DOCUMENT",
            contextMode: "SINGLE_DOCUMENT",
            lastMessageAt: null,
            openedAt: 1699999999000,
            scope: "PORTAL",
            sessionId: "3002",
            status: "OPEN",
            title: "王圻官制"
        });
        mocks.createQaChatCompletionStream.mockResolvedValueOnce({
            answerStatus: "SUCCEEDED",
            choices: [
                {
                    index: 0,
                    message: { content: "王圻文档答案", role: "assistant" },
                    finishReason: "stop"
                }
            ],
            sessionId: "3002"
        });

        const { container, root } = renderPage(
            "/discovery/qa?contextContentType=WANGQI_DOCUMENT&contextContentId=3001&contextMode=SINGLE_DOCUMENT&title=%E7%8E%8B%E5%9C%BB%E5%AE%98%E5%88%B6"
        );
        await flushAsyncWork();
        setTextareaValue(container, "question", "这份文档说了什么？");

        const submitButton = container.querySelector(
            'button[type="submit"]'
        ) as HTMLButtonElement | null;
        expect(submitButton).not.toBeNull();
        await act(async () => {
            submitButton?.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(mocks.openQaSession.mock.calls[0]?.[0]).toMatchObject({
            contextContentId: 3001,
            contextContentType: "WANGQI_DOCUMENT",
            contextMode: "SINGLE_DOCUMENT",
            title: "王圻官制"
        });
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
            request: {
                sessionId: "3002",
                metadata: {
                    contextContentId: 3001,
                    contextContentType: "WANGQI_DOCUMENT",
                    contextMode: "SINGLE_DOCUMENT",
                    sessionId: "3002"
                }
            }
        });

        act(() => {
            root.unmount();
        });
    });

    it("confirms delete and clears selected session after deletion", async () => {
        mocks.pageQaSessions.mockResolvedValue({
            items: [
                {
                    lastMessageAt: 1700000000000,
                    scope: "PORTAL",
                    sessionId: "5001",
                    status: "OPEN",
                    title: "待删除会话"
                }
            ]
        });
        mocks.getQaSession.mockResolvedValue({
            sessionId: "5001",
            status: "OPEN",
            title: "待删除会话"
        });
        mocks.deleteQaSession.mockResolvedValue(undefined);
        const confirmSpy = vi.spyOn(window, "confirm").mockReturnValue(true);

        const { container, root } = renderPage();
        await flushAsyncWork();
        await flushAsyncWork();

        const sessionButton = findButtonByText(container, "待删除会话");
        await act(async () => {
            sessionButton.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        const deleteButton = findButtonByText(container, "删除会话");
        await act(async () => {
            deleteButton.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(confirmSpy).toHaveBeenCalledWith("确认删除会话 5001？");
        expect(mocks.deleteQaSession.mock.calls[0]?.[0]).toEqual({
            ownerUserId: 1001,
            sessionId: "5001"
        });
        expect(container.textContent).toContain("会话 5001 已删除");
        expect(container.textContent).toContain("已选会话 未选择");

        confirmSpy.mockRestore();
        act(() => {
            root.unmount();
        });
    });

    it("shows export success and failure feedback", async () => {
        mocks.pageQaSessions.mockResolvedValue({
            items: [
                {
                    scope: "PORTAL",
                    sessionId: "5002",
                    status: "OPEN",
                    title: "可导出会话"
                }
            ]
        });
        mocks.getQaSession.mockResolvedValue({
            sessionId: "5002",
            status: "OPEN",
            title: "可导出会话"
        });
        mocks.exportQaSession
            .mockResolvedValueOnce({
                exportId: 7001,
                exportStatus: "SUCCEEDED",
                filename: "discovery-qa-session-5002-7001.csv",
                sessionId: "5002",
                storageObjectId: 8001
            })
            .mockResolvedValueOnce({
                exportId: 7002,
                exportStatus: "FAILED",
                failureReason: "storage unavailable",
                sessionId: "5002"
            });

        const { container, root } = renderPage();
        await flushAsyncWork();
        await flushAsyncWork();

        const sessionButton = findButtonByText(container, "可导出会话");
        await act(async () => {
            sessionButton.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        const exportButton = findButtonByText(container, "导出 CSV");
        await act(async () => {
            exportButton.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(mocks.exportQaSession.mock.calls[0]?.[0]).toEqual({
            format: "CSV",
            ownerUserId: 1001,
            sessionId: "5002"
        });
        expect(container.textContent).toContain("导出成功：discovery-qa-session-5002-7001.csv");
        expect(container.textContent).toContain("对象号 8001");

        await act(async () => {
            exportButton.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(container.textContent).toContain("storage unavailable");

        act(() => {
            root.unmount();
        });
    });

    it("reuses selected session for follow-up questions", async () => {
        mocks.pageQaSessions.mockResolvedValue({
            items: [
                {
                    contextContentId: 2002,
                    contextContentType: "SANCAI_ENTRY",
                    contextMode: "GENERAL",
                    lastMessageAt: 1700000000000,
                    openedAt: 1699999998000,
                    scope: "PORTAL",
                    sessionId: "5001",
                    status: "OPEN",
                    title: "古籍问答"
                }
            ]
        });
        mocks.getQaSession.mockResolvedValue({
            contextContentId: 2002,
            contextContentType: "SANCAI_ENTRY",
            contextMode: "GENERAL",
            sessionId: "5001",
            status: "OPEN",
            title: "古籍问答"
        });
        mocks.createQaChatCompletionStream
            .mockResolvedValueOnce({
                answerStatus: "SUCCEEDED",
                choices: [
                    {
                        index: 0,
                        message: { content: "首问答案", role: "assistant" },
                        finishReason: "stop"
                    }
                ],
                sessionId: "5001"
            })
            .mockResolvedValueOnce({
                answerStatus: "SUCCEEDED",
                choices: [
                    {
                        index: 0,
                        message: { content: "复问答案", role: "assistant" },
                        finishReason: "stop"
                    }
                ],
                sessionId: "5001"
            });

        const { container, root } = renderPage();
        await flushAsyncWork();
        await flushAsyncWork();

        const firstSessionButton = findButtonByText(container, "古籍问答");
        await act(async () => {
            firstSessionButton.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        setTextareaValue(container, "question", "礼器第一问");
        const submitButton = container.querySelector(
            'button[type="submit"]'
        ) as HTMLButtonElement | null;
        expect(submitButton).not.toBeNull();

        await act(async () => {
            submitButton?.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(mocks.openQaSession).not.toHaveBeenCalled();
        expect(mocks.createQaChatCompletionStream).toHaveBeenCalledTimes(1);
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
            request: {
                sessionId: "5001",
                metadata: {
                    contextContentId: 2002,
                    contextContentType: "SANCAI_ENTRY",
                    contextMode: "GENERAL",
                    sessionId: "5001"
                }
            }
        });

        setTextareaValue(container, "question", "礼器复问");
        await act(async () => {
            submitButton?.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(mocks.createQaChatCompletionStream).toHaveBeenCalledTimes(2);
        expect(mocks.createQaChatCompletionStream.mock.calls[1]![0]).toMatchObject({
            request: {
                sessionId: "5001",
                metadata: {
                    contextContentId: 2002,
                    contextContentType: "SANCAI_ENTRY",
                    contextMode: "GENERAL",
                    sessionId: "5001"
                }
            }
        });

        act(() => {
            root.unmount();
        });
    });

    it("renders unavailable source without link and supports retryable answer failure", async () => {
        mocks.pageQaSessions.mockResolvedValue({
            items: []
        });
        mocks.getQaSession.mockResolvedValue({
            sessionId: "2002",
            status: "OPEN",
            title: "知识中心问答"
        });
        mocks.openQaSession.mockResolvedValue({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            lastMessageAt: null,
            openedAt: 1699999999000,
            scope: "PORTAL",
            sessionId: "2002",
            status: "OPEN",
            title: "知识中心问答"
        });
        mocks.createQaChatCompletionStream
            .mockRejectedValueOnce(new Error("provider down"))
            .mockResolvedValueOnce({
                answerStatus: "SUCCEEDED",
                choices: [
                    {
                        index: 0,
                        message: { content: "重试后恢复", role: "assistant" },
                        finishReason: "stop"
                    }
                ],
                sessionId: "2002",
                sources: [
                    {
                        contentId: 1001,
                        contentType: "SANCAI_ENTRY",
                        sourceId: "SANCAI_ENTRY:1001",
                        sourceStatus: "UNAVAILABLE",
                        titleSnapshot: "礼器条目",
                        snippet: "失效来源"
                    }
                ]
            });

        const { container, root } = renderPage();
        setTextareaValue(container, "question", "礼器是否可见？");

        const submitButton = container.querySelector(
            'button[type="submit"]'
        ) as HTMLButtonElement | null;
        expect(submitButton).not.toBeNull();
        await act(async () => {
            submitButton?.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(container.textContent).toContain("发送失败，请重试");
        const retryButton = findButtonByText(container, "重试");
        await act(async () => {
            retryButton.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(mocks.createQaChatCompletionStream).toHaveBeenCalledTimes(2);
        const sourceText = container.textContent ?? "";
        expect(sourceText).toContain("礼器条目");
        expect(container.querySelector(".portal-qa-source-list a")).toBeNull();

        act(() => {
            root.unmount();
        });
    });

    it("refreshes selected session detail", async () => {
        mocks.pageQaSessions.mockResolvedValue({
            items: [
                {
                    scope: "PORTAL",
                    sessionId: "6001",
                    status: "OPEN",
                    title: "可刷新会话"
                }
            ]
        });
        mocks.getQaSession
            .mockResolvedValueOnce({
                lastMessageAt: 1700000000000,
                sessionId: "6001",
                status: "OPEN",
                title: "可刷新会话"
            })
            .mockResolvedValueOnce({
                lastMessageAt: 1700000100000,
                sessionId: "6001",
                status: "OPEN",
                title: "可刷新会话"
            });

        const { container, root } = renderPage();
        await flushAsyncWork();
        await flushAsyncWork();

        const sessionButton = findButtonByText(container, "可刷新会话");
        await act(async () => {
            sessionButton.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });
        await flushAsyncWork();

        const refreshButton = findButtonByText(container, "刷新详情");
        await act(async () => {
            refreshButton.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(mocks.getQaSession).toHaveBeenCalledTimes(2);
        expect(container.textContent).toContain("会话 6001 详情已刷新");

        act(() => {
            root.unmount();
        });
    });

    it("locks portal qa service to Discovery APIs without provider direct urls", async () => {
        const qaService = await vi.importActual<typeof import("./qa-service")>("./qa-service");

        await qaService.openQaSession({ scope: "PORTAL", title: "知识中心问答" });
        await qaService.pageQaSessions({
            ownerUserId: 1001,
            pageNo: 1,
            pageSize: 10,
            scope: "PORTAL"
        });
        await qaService.getQaSession({ ownerUserId: 1001, sessionId: "2001" });
        await qaService.deleteQaSession({ ownerUserId: 1001, sessionId: "2001" });
        await qaService.exportQaSession({ format: "CSV", ownerUserId: 1001, sessionId: "2001" });
        mocks.postEventStream.mockImplementationOnce(async (_url, options) => {
            options.onChunk(
                'event:completed\ndata:{"sessionId":"2001","choices":[{"message":{"content":"礼器答案"}}]}\n\n'
            );
        });
        await qaService.createQaChatCompletionStream({
            request: {
                messages: [{ content: "礼器是什么？", role: "user" }],
                metadata: { sessionId: "2001" },
                model: "kuzhambu-qa",
                sessionId: "2001",
                stream: true
            }
        });

        const calledUrls = mocks.postJson.mock.calls.map(([url]) => String(url));
        const calledStreamUrls = mocks.postEventStream.mock.calls.map(([url]) => String(url));
        expect(calledStreamUrls).toContain("/portal/discovery/qa/chat/completions/stream");
        expect(calledUrls).toContain("/portal/discovery/qa/session/delete");
        expect(calledUrls).toContain("/portal/discovery/qa/session/export");
        expect([...calledUrls, ...calledStreamUrls].join("\n")).not.toContain(
            "/portal/discovery/qa/question/ask"
        );
        expect([...calledUrls, ...calledStreamUrls].join("\n")).not.toMatch(
            /https?:\/\/|fastgpt|dataset|collection|appId|baseUrl/iu
        );
    });
});
