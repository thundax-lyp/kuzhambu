import { createRoot } from "react-dom/client";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { DiscoveryQaPage } from "./qa-page";

const mocks = vi.hoisted(() => ({
    submitChatCompletion: vi.fn(),
    deleteQaSession: vi.fn(),
    downloadQaSession: vi.fn(),
    getQaSession: vi.fn(),
    initQaSession: vi.fn(),
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

const flushAsyncWork = async () => {
    await act(async () => {
        await new Promise((resolve) => setTimeout(resolve, 0));
    });
};

describe("DiscoveryQaPage", () => {
    afterEach(() => {
        mocks.submitChatCompletion.mockReset();
        mocks.deleteQaSession.mockReset();
        mocks.downloadQaSession.mockReset();
        mocks.getQaSession.mockReset();
        mocks.initQaSession.mockReset();
        mocks.pageQaSessions.mockReset();
        mocks.postEventStream.mockReset();
        mocks.postJson.mockReset();
        localStorage.clear();
        document.body.innerHTML = "";
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
        mocks.initQaSession.mockResolvedValueOnce({
            contextContentId: 3001,
            contextContentType: "WANGQI_DOCUMENT",
            contextMode: "SINGLE_DOCUMENT",
            lastMessageAt: null,
            openedAt: 1699999999000,
            scope: "PORTAL",
            id: "3002",
            status: "OPEN",
            title: "王圻官制"
        });
        mocks.submitChatCompletion.mockResolvedValueOnce({
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

        expect(mocks.initQaSession.mock.calls[0]?.[0]).toMatchObject({
            contextContentId: 3001,
            contextContentType: "WANGQI_DOCUMENT",
            contextMode: "SINGLE_DOCUMENT",
            title: "王圻官制"
        });
        expect(mocks.submitChatCompletion.mock.calls[0]?.[0]).toMatchObject({
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

    it("locks portal qa service to Discovery APIs without provider direct urls", async () => {
        const qaService = await vi.importActual<typeof import("./qa-service")>("./qa-service");

        await qaService.initQaSession({ scope: "PORTAL", title: "知识中心问答" });
        await qaService.pageQaSessions({
            ownerUserId: 1001,
            pageNo: 1,
            pageSize: 10,
            scope: "PORTAL"
        });
        await qaService.getQaSession({ ownerUserId: 1001, sessionId: "2001" });
        await qaService.deleteQaSession({ ownerUserId: 1001, sessionId: "2001" });
        await qaService.downloadQaSession({ format: "CSV", ownerUserId: 1001, sessionId: "2001" });
        mocks.postEventStream.mockImplementationOnce(async (_url, options) => {
            options.onChunk(
                'event:completed\ndata:{"sessionId":"2001","choices":[{"message":{"content":"礼器答案"}}]}\n\n'
            );
        });
        await qaService.submitChatCompletion({
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
        expect(calledStreamUrls).toContain("/portal/discovery/qa/chat/submit");
        expect(calledUrls).toContain("/portal/discovery/qa/session/delete");
        expect(calledUrls).toContain("/portal/discovery/qa/session/download");
        expect([...calledUrls, ...calledStreamUrls].join("\n")).not.toContain(
            "/portal/discovery/qa/question/ask"
        );
        expect([...calledUrls, ...calledStreamUrls].join("\n")).not.toMatch(
            /https?:\/\/|fastgpt|dataset|collection|appId|baseUrl/iu
        );
    });
});
