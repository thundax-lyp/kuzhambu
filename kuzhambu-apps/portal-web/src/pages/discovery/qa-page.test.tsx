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

const createDeferred = <T,>() => {
    let resolve!: (value: T) => void;
    let reject!: (reason?: unknown) => void;
    const promise = new Promise<T>((promiseResolve, promiseReject) => {
        resolve = promiseResolve;
        reject = promiseReject;
    });

    return { promise, reject, resolve };
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
        expect(container.textContent).toContain("礼器条目");
        expect(container.querySelectorAll(".portal-qa-avatar")).toHaveLength(2);
        expect(container.querySelector('a[href="/shares/1001"]')).not.toBeNull();

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
            sessionId: "2101",
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

    it("prevents duplicate session creation while first open request is pending", async () => {
        const openResponse = {
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            lastMessageAt: null,
            openedAt: 1699999999000,
            scope: "PORTAL",
            sessionId: "pending-2101",
            status: "OPEN",
            title: "知识中心问答"
        };
        const pendingOpen = createDeferred<typeof openResponse>();
        mocks.openQaSession.mockReturnValueOnce(pendingOpen.promise);
        mocks.createQaChatCompletionStream.mockResolvedValueOnce({
            answerStatus: "SUCCEEDED",
            choices: [
                {
                    finishReason: "stop",
                    index: 0,
                    message: {
                        content: "只回答一次。",
                        role: "assistant"
                    }
                }
            ],
            sessionId: "pending-2101"
        });

        const { container, root } = renderPage();

        setTextareaValue(container, "question", "不要重复创建");
        await pressTextareaKey(container, "question", "Enter");
        await pressTextareaKey(container, "question", "Enter");

        expect(mocks.openQaSession).toHaveBeenCalledTimes(1);
        expect(mocks.createQaChatCompletionStream).not.toHaveBeenCalled();

        await act(async () => {
            pendingOpen.resolve(openResponse);
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(mocks.createQaChatCompletionStream).toHaveBeenCalledTimes(1);
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
            request: {
                messages: [{ content: "不要重复创建", role: "user" }],
                sessionId: "pending-2101"
            }
        });
        expect(container.textContent).toContain("只回答一次。");

        act(() => {
            root.unmount();
        });
    });

    it("reuses local session for thirty minutes without showing session management", async () => {
        localStorage.setItem(
            "kuzhambu.portal.discovery.qa.session",
            JSON.stringify({
                contextKey: "PORTAL|1001|GENERAL||",
                expiresAt: Date.now() + 30 * 60 * 1000,
                session: {
                    contextContentId: null,
                    contextContentType: null,
                    contextMode: "GENERAL",
                    scope: "PORTAL",
                    sessionId: "stored-2001",
                    status: "OPEN",
                    title: "知识中心问答"
                }
            })
        );
        mocks.createQaChatCompletionStream.mockResolvedValueOnce({
            answerStatus: "SUCCEEDED",
            choices: [
                {
                    finishReason: "stop",
                    index: 0,
                    message: {
                        content: "继续回答。",
                        role: "assistant"
                    }
                }
            ],
            sessionId: "stored-2001"
        });

        const { container, root } = renderPage();

        setTextareaValue(container, "question", "继续问");
        await pressTextareaKey(container, "question", "Enter");

        expect(mocks.openQaSession).not.toHaveBeenCalled();
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
            request: {
                messages: [{ content: "继续问", role: "user" }],
                sessionId: "stored-2001"
            }
        });
        expect(container.textContent).not.toContain("会话列表");
        expect(container.textContent).toContain("继续回答。");

        act(() => {
            root.unmount();
        });
    });

    it("clears unavailable cached session and retries with a new session", async () => {
        localStorage.setItem(
            "kuzhambu.portal.discovery.qa.session",
            JSON.stringify({
                contextKey: "PORTAL|1001|GENERAL||",
                expiresAt: Date.now() + 30 * 60 * 1000,
                session: {
                    contextContentId: null,
                    contextContentType: null,
                    contextMode: "GENERAL",
                    scope: "PORTAL",
                    sessionId: "removed-2001",
                    status: "OPEN",
                    title: "知识中心问答"
                }
            })
        );
        mocks.createQaChatCompletionStream
            .mockRejectedValueOnce(new Error("Portal stream request failed: 404"))
            .mockResolvedValueOnce({
                answerStatus: "SUCCEEDED",
                choices: [
                    {
                        finishReason: "stop",
                        index: 0,
                        message: {
                            content: "新会话恢复回答。",
                            role: "assistant"
                        }
                    }
                ],
                sessionId: "recovered-2001"
            });
        mocks.openQaSession.mockResolvedValueOnce({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            scope: "PORTAL",
            sessionId: "recovered-2001",
            status: "OPEN",
            title: "知识中心问答"
        });

        const { container, root } = renderPage();

        setTextareaValue(container, "question", "旧会话还能用吗？");
        await pressTextareaKey(container, "question", "Enter");

        expect(mocks.openQaSession).not.toHaveBeenCalled();
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
            request: {
                messages: [{ content: "旧会话还能用吗？", role: "user" }],
                sessionId: "removed-2001"
            }
        });
        expect(localStorage.getItem("kuzhambu.portal.discovery.qa.session")).toBeNull();
        expect(container.textContent).toContain("当前会话已失效，请重新发送问题。");

        const retryButton = findButtonByText(container, "重试");
        await act(async () => {
            retryButton.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(mocks.openQaSession).toHaveBeenCalledTimes(1);
        expect(mocks.createQaChatCompletionStream.mock.calls[1]?.[0]).toMatchObject({
            request: {
                messages: [{ content: "旧会话还能用吗？", role: "user" }],
                sessionId: "recovered-2001"
            }
        });
        expect(container.textContent).toContain("新会话恢复回答。");

        act(() => {
            root.unmount();
        });
    });

    it("opens a new session when the local session is expired", async () => {
        localStorage.setItem(
            "kuzhambu.portal.discovery.qa.session",
            JSON.stringify({
                contextKey: "PORTAL|1001|GENERAL||",
                expiresAt: Date.now() - 1,
                session: {
                    contextMode: "GENERAL",
                    scope: "PORTAL",
                    sessionId: "expired-2001",
                    status: "OPEN",
                    title: "知识中心问答"
                }
            })
        );
        mocks.openQaSession.mockResolvedValueOnce({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            scope: "PORTAL",
            sessionId: "new-2001",
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
                        content: "新会话回答。",
                        role: "assistant"
                    }
                }
            ],
            sessionId: "new-2001"
        });

        const { container, root } = renderPage();

        setTextareaValue(container, "question", "重新开始");
        await pressTextareaKey(container, "question", "Enter");

        expect(mocks.openQaSession).toHaveBeenCalledTimes(1);
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
            request: {
                messages: [{ content: "重新开始", role: "user" }],
                sessionId: "new-2001"
            }
        });
        expect(container.textContent).toContain("新会话回答。");

        act(() => {
            root.unmount();
        });
    });

    it("restores Wangqi single document context from url", async () => {
        const { container, root } = renderPage(
            "/discovery/qa?contextContentType=WANGQI_DOCUMENT&contextContentId=3001&contextMode=SINGLE_DOCUMENT&title=%E7%8E%8B%E5%9C%BB%E5%AE%98%E5%88%B6"
        );
        await flushAsyncWork();

        expect(container.textContent).toContain("当前文档");
        expect(container.textContent).toContain("WANGQI_DOCUMENT #3001");
        expect(container.querySelectorAll("input")).toHaveLength(0);
        expect(container.textContent).not.toContain("会话元数据");

        act(() => {
            root.unmount();
        });
    });

    it("sends Wangqi single document context in open session and chat metadata", async () => {
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

    it("renders unavailable source without link and supports retryable answer failure", async () => {
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
