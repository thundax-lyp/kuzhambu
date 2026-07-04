import { createRoot } from "react-dom/client";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { DiscoveryQaPage } from "./qa-page";

const mocks = vi.hoisted(() => ({
    createQaChatCompletion: vi.fn(),
    deleteQaSession: vi.fn(),
    exportQaSession: vi.fn(),
    getQaSession: vi.fn(),
    openQaSession: vi.fn(),
    pageQaSessions: vi.fn(),
    postJson: vi.fn()
}));

vi.mock("@/api/http", () => ({
    postJson: mocks.postJson
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
        mocks.createQaChatCompletion.mockReset();
        mocks.deleteQaSession.mockReset();
        mocks.exportQaSession.mockReset();
        mocks.getQaSession.mockReset();
        mocks.openQaSession.mockReset();
        mocks.pageQaSessions.mockReset();
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
        expect(mocks.createQaChatCompletion.mock.calls[0]?.[0]).toMatchObject({
            model: "kuzhambu-qa",
            messages: [{ content: "礼器是什么？", role: "user" }],
            metadata: { contextContentId: null, contextContentType: null, sessionId: 2001 },
            stream: false
        });
        expect(container.textContent).toContain("礼器常见于典章与礼仪条目。");
        expect(container.textContent).toContain("礼器条目");
        expect(container.querySelector('a[href="/shares/1001"]')).not.toBeNull();

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
                    sessionId: 5001,
                    status: "OPEN",
                    title: "待删除会话"
                }
            ]
        });
        mocks.getQaSession.mockResolvedValue({
            sessionId: 5001,
            status: "OPEN",
            title: "待删除会话"
        });
        mocks.deleteQaSession.mockResolvedValue(undefined);
        const confirmSpy = vi.spyOn(window, "confirm").mockReturnValue(true);

        const { container, root } = renderPage();
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
        expect(mocks.deleteQaSession).toHaveBeenCalledWith({
            ownerUserId: 1001,
            sessionId: 5001
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
                    sessionId: 5002,
                    status: "OPEN",
                    title: "可导出会话"
                }
            ]
        });
        mocks.getQaSession.mockResolvedValue({
            sessionId: 5002,
            status: "OPEN",
            title: "可导出会话"
        });
        mocks.exportQaSession
            .mockResolvedValueOnce({
                exportId: 7001,
                exportStatus: "SUCCEEDED",
                filename: "discovery-qa-session-5002-7001.csv",
                sessionId: 5002,
                storageObjectId: 8001
            })
            .mockResolvedValueOnce({
                exportId: 7002,
                exportStatus: "FAILED",
                failureReason: "storage unavailable",
                sessionId: 5002
            });

        const { container, root } = renderPage();
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

        expect(mocks.exportQaSession).toHaveBeenCalledWith({
            format: "CSV",
            ownerUserId: 1001,
            sessionId: 5002
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
        expect(mocks.createQaChatCompletion).toHaveBeenCalledTimes(1);
        expect(mocks.createQaChatCompletion.mock.calls[0]?.[0]).toMatchObject({
            metadata: { sessionId: 5001 }
        });

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
        mocks.pageQaSessions.mockResolvedValue({
            items: []
        });
        mocks.getQaSession.mockResolvedValue({
            sessionId: 2002,
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
        await qaService.getQaSession({ ownerUserId: 1001, sessionId: 2001 });
        await qaService.deleteQaSession({ ownerUserId: 1001, sessionId: 2001 });
        await qaService.exportQaSession({ format: "CSV", ownerUserId: 1001, sessionId: 2001 });
        await qaService.createQaChatCompletion({
            messages: [{ content: "礼器是什么？", role: "user" }],
            metadata: { sessionId: 2001 },
            model: "kuzhambu-qa",
            stream: false
        });

        const calledUrls = mocks.postJson.mock.calls.map(([url]) => String(url));
        expect(calledUrls).toContain("/portal/discovery/qa/chat/completions");
        expect(calledUrls).toContain("/portal/discovery/qa/session/delete");
        expect(calledUrls).toContain("/portal/discovery/qa/session/export");
        expect(calledUrls.join("\n")).not.toContain("/portal/discovery/qa/question/ask");
        expect(calledUrls.join("\n")).not.toMatch(
            /https?:\/\/|fastgpt|dataset|collection|appId|baseUrl/iu
        );
    });
});
