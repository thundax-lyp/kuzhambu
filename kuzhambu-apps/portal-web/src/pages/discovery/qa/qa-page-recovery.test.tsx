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

    it("clears cached session when validation confirms it is unavailable", async () => {
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
        mocks.getQaSession.mockRejectedValueOnce(new Error("Portal API request failed: 404"));
        mocks.openQaSession.mockResolvedValueOnce({
            contextContentId: null,
            contextContentType: null,
            contextMode: "GENERAL",
            scope: "PORTAL",
            sessionId: "recovered-2001",
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
                        content: "新会话恢复回答。",
                        role: "assistant"
                    }
                }
            ],
            sessionId: "recovered-2001"
        });

        const { container, root } = renderPage();

        setTextareaValue(container, "question", "旧会话还能用吗？");
        await pressTextareaKey(container, "question", "Enter");

        expect(mocks.getQaSession).toHaveBeenCalledWith({
            ownerUserId: 1001,
            sessionId: "removed-2001"
        });
        expect(localStorage.getItem("kuzhambu.portal.discovery.qa.session")).not.toContain(
            "removed-2001"
        );
        expect(mocks.openQaSession).toHaveBeenCalledTimes(1);
        expect(mocks.createQaChatCompletionStream.mock.calls[0]?.[0]).toMatchObject({
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
});
