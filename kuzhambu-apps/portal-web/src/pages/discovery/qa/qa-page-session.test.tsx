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

    it("prevents duplicate session creation while first open request is pending", async () => {
        const openResponse = {
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            lastMessageAt: null,
            openedAt: 1699999999000,
            scope: "PORTAL",
            id: "2101",
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
            sessionId: "2101"
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
                sessionId: "2101"
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
                    id: "2001",
                    scope: "PORTAL",
                    status: "OPEN",
                    title: "知识中心问答"
                }
            })
        );
        mocks.getQaSession.mockResolvedValueOnce({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            id: "2001",
            scope: "PORTAL",
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
                        content: "继续回答。",
                        role: "assistant"
                    }
                }
            ],
            sessionId: "2001"
        });

        const { container, root } = renderPage();

        setTextareaValue(container, "question", "继续问");
        await pressTextareaKey(container, "question", "Enter");

        expect(mocks.getQaSession).toHaveBeenCalledWith({
            ownerUserId: 1001,
            sessionId: "2001"
        });
        expect(mocks.openQaSession).not.toHaveBeenCalled();
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
            request: {
                messages: [{ content: "继续问", role: "user" }],
                sessionId: "2001"
            }
        });
        expect(container.textContent).not.toContain("会话列表");
        expect(container.textContent).toContain("继续回答。");

        act(() => {
            root.unmount();
        });
    });

    it("keeps validated cached session on generic stream failure and retries same session", async () => {
        localStorage.setItem(
            "kuzhambu.portal.discovery.qa.session",
            JSON.stringify({
                contextKey: "PORTAL|1001|GENERAL||",
                expiresAt: Date.now() + 30 * 60 * 1000,
                session: {
                    contextContentId: null,
                    contextContentType: null,
                    contextMode: "GENERAL",
                    id: "2201",
                    scope: "PORTAL",
                    status: "OPEN",
                    title: "知识中心问答"
                }
            })
        );
        mocks.getQaSession.mockResolvedValueOnce({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            id: "2201",
            scope: "PORTAL",
            status: "OPEN",
            title: "知识中心问答"
        });
        mocks.createQaChatCompletionStream
            .mockRejectedValueOnce(new Error("问答生成失败，请稍后重试。"))
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
                sessionId: "2201"
            });

        const { container, root } = renderPage();

        setTextareaValue(container, "question", "旧会话还能用吗？");
        await pressTextareaKey(container, "question", "Enter");

        expect(mocks.openQaSession).not.toHaveBeenCalled();
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
            request: {
                messages: [{ content: "旧会话还能用吗？", role: "user" }],
                sessionId: "2201"
            }
        });
        expect(localStorage.getItem("kuzhambu.portal.discovery.qa.session")).not.toBeNull();
        expect(container.textContent).toContain("发送失败，请重试");

        const retryButton = findButtonByText(container, "重试");
        await act(async () => {
            retryButton.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(mocks.openQaSession).not.toHaveBeenCalled();
        expect(mocks.createQaChatCompletionStream.mock.calls[1]?.[0]).toMatchObject({
            request: {
                messages: [{ content: "旧会话还能用吗？", role: "user" }],
                sessionId: "2201"
            }
        });
        expect(container.textContent).toContain("新会话恢复回答。");

        act(() => {
            root.unmount();
        });
    });

    it("opens a new session instead of reusing stale nonnumeric local session id", async () => {
        localStorage.setItem(
            "kuzhambu.portal.discovery.qa.session",
            JSON.stringify({
                contextKey: "PORTAL|1001|GENERAL||",
                expiresAt: Date.now() + 30 * 60 * 1000,
                session: {
                    contextContentId: null,
                    contextContentType: null,
                    contextMode: "GENERAL",
                    id: "stored-legacy",
                    scope: "PORTAL",
                    status: "OPEN",
                    title: "知识中心问答"
                }
            })
        );
        mocks.openQaSession.mockResolvedValueOnce({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            id: "2301",
            scope: "PORTAL",
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
            sessionId: "2301"
        });

        const { container, root } = renderPage();

        setTextareaValue(container, "question", "旧缓存还能用吗？");
        await pressTextareaKey(container, "question", "Enter");

        expect(mocks.getQaSession).not.toHaveBeenCalled();
        expect(mocks.openQaSession).toHaveBeenCalledTimes(1);
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
            request: {
                messages: [{ content: "旧缓存还能用吗？", role: "user" }],
                sessionId: "2301"
            }
        });
        expect(container.textContent).toContain("新会话回答。");

        act(() => {
            root.unmount();
        });
    });
});
