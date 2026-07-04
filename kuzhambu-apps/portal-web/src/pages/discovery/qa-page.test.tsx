import { createRoot } from "react-dom/client";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { DiscoveryQaPage } from "./qa-page";

const mocks = vi.hoisted(() => ({
    createQaChatCompletion: vi.fn(),
    getQaSession: vi.fn(),
    openQaSession: vi.fn(),
    pageQaSessions: vi.fn()
}));

vi.mock("./qa-service", () => mocks);

const renderPage = () => {
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
                <MemoryRouter>
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

    expect(button).not.toBeNull();
    return button as HTMLButtonElement;
};

describe("DiscoveryQaPage", () => {
    afterEach(() => {
        mocks.createQaChatCompletion.mockReset();
        mocks.getQaSession.mockReset();
        mocks.openQaSession.mockReset();
        mocks.pageQaSessions.mockReset();
        document.body.innerHTML = "";
    });

    it("auto opens session on first question and then sends chat/completions", async () => {
        mocks.pageQaSessions.mockResolvedValueOnce({
            items: []
        });
        mocks.openQaSession.mockResolvedValueOnce({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            lastMessageAt: null,
            openedAt: 1699999999000,
            scope: "PORTAL",
            sessionId: 2001,
            status: "OPEN",
            title: "知识中心问答"
        });
        mocks.createQaChatCompletion.mockResolvedValueOnce({
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
            sessionId: 2001
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

        expect(mocks.openQaSession).toHaveBeenCalledWith({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            requestId: null,
            scope: "PORTAL",
            title: "知识中心问答",
            traceId: null
        });
        expect(mocks.createQaChatCompletion).toHaveBeenCalledWith(
            expect.objectContaining({
                model: "kuzhambu-qa",
                messages: [{ content: "礼器是什么？", role: "user" }],
                metadata: { contextContentId: null, contextContentType: null, sessionId: 2001 },
                stream: false
            })
        );
        expect(mocks.createQaChatCompletion).not.toHaveBeenCalledWith(
            expect.objectContaining({
                model: "kuzhambu-qa",
                stream: false,
                metadata: { sessionId: 2001 }
            })
        );
        expect(container.textContent).toContain("礼器常见于典章与礼仪条目。");
        expect(container.textContent).toContain("礼器条目");
        expect(container.querySelector('a[href="/shares/1001"]')).not.toBeNull();

        act(() => {
            root.unmount();
        });
    });

    it("reuses selected session for follow-up questions", async () => {
        mocks.pageQaSessions.mockResolvedValueOnce({
            items: [
                {
                    contextContentId: 2002,
                    contextContentType: "SANCAI_ENTRY",
                    contextMode: "GENERAL",
                    lastMessageAt: 1700000000000,
                    openedAt: 1699999998000,
                    scope: "PORTAL",
                    sessionId: 5001,
                    status: "OPEN",
                    title: "古籍问答"
                }
            ]
        });
        mocks.getQaSession.mockResolvedValue({
            sessionId: 5001,
            status: "OPEN",
            title: "古籍问答"
        });
        mocks.createQaChatCompletion
            .mockResolvedValueOnce({
                answerStatus: "SUCCEEDED",
                choices: [
                    {
                        index: 0,
                        message: { content: "首问答案", role: "assistant" },
                        finishReason: "stop"
                    }
                ],
                sessionId: 5001
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
                sessionId: 5001
            });

        const { container, root } = renderPage();
        await act(async () => {
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

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
        expect(mocks.createQaChatCompletion).toHaveBeenCalledTimes(1);
        expect(mocks.createQaChatCompletion).toHaveBeenLastCalledWith(
            expect.objectContaining({ metadata: { sessionId: 5001 } })
        );

        setTextareaValue(container, "question", "礼器复问");
        await act(async () => {
            submitButton?.click();
            await new Promise((resolve) => setTimeout(resolve, 0));
        });

        expect(mocks.createQaChatCompletion).toHaveBeenCalledTimes(2);
        expect(mocks.createQaChatCompletion.mock.calls[1]![0]).toMatchObject({
            metadata: { sessionId: 5001 }
        });

        act(() => {
            root.unmount();
        });
    });

    it("renders unavailable source without link and supports retryable answer failure", async () => {
        mocks.pageQaSessions.mockResolvedValueOnce({
            items: []
        });
        mocks.openQaSession.mockResolvedValue({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            lastMessageAt: null,
            openedAt: 1699999999000,
            scope: "PORTAL",
            sessionId: 2002,
            status: "OPEN",
            title: "知识中心问答"
        });
        mocks.createQaChatCompletion
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
                sessionId: 2002,
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

        expect(mocks.createQaChatCompletion).toHaveBeenCalledTimes(2);
        const sourceText = container.textContent ?? "";
        expect(sourceText).toContain("礼器条目");
        expect(container.querySelector("a")).toBeNull();

        act(() => {
            root.unmount();
        });
    });
});
