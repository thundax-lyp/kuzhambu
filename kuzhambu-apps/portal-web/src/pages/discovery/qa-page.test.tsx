import { createRoot } from "react-dom/client";
import { act } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { DiscoveryQaPage } from "./qa-page";

const mocks = vi.hoisted(() => ({
    askQaQuestion: vi.fn(),
    openQaSession: vi.fn()
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

const setInputValue = (container: HTMLElement, name: string, value: string) => {
    const input = container.querySelector(`input[name="${name}"]`) as HTMLInputElement | null;
    expect(input).not.toBeNull();

    act(() => {
        if (!input) {
            return;
        }

        const setter = Object.getOwnPropertyDescriptor(
            window.HTMLInputElement.prototype,
            "value"
        )?.set;
        setter?.call(input, value);
        input.dispatchEvent(new Event("input", { bubbles: true }));
    });
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

describe("DiscoveryQaPage", () => {
    afterEach(() => {
        mocks.askQaQuestion.mockReset();
        mocks.openQaSession.mockReset();
        document.body.innerHTML = "";
    });

    it("opens a session and renders qa answer sources", async () => {
        mocks.openQaSession.mockResolvedValueOnce({
            lastMessageAt: 1700000000000,
            openedAt: 1699990000000,
            ownerUserId: 1,
            scope: "portal",
            sessionId: 2001,
            status: "OPEN",
            title: "知识中心问答"
        });
        mocks.askQaQuestion.mockResolvedValueOnce({
            answer: "礼器通常出现在礼制、器物和制度相关章节。",
            answerMessageId: 4002,
            answerStatus: "SUCCESS",
            question: "礼器在哪里出现？",
            questionMessageId: 4001,
            sessionId: 2001,
            sources: [
                {
                    contentId: 1001,
                    contentType: "SANCAI_ENTRY",
                    knowledgeBase: "classics",
                    locationLabel: "卷一",
                    score: 0.9123,
                    sourceId: 5001,
                    sourceRank: 1,
                    sourceStatus: "ACTIVE",
                    snippet: "礼器，礼之所用也。",
                    titleSnapshot: "礼器条目"
                }
            ],
            traceSummary: {
                candidateCount: 2,
                expandedTermsJson: '["礼器","礼制"]',
                linkedEntitiesJson: '["器物"]',
                rewrittenQuestion: "礼器的章节位置是什么？",
                traceId: 9001
            }
        });

        const { container, root } = renderPage();

        setInputValue(container, "ownerUserId", "1");
        setInputValue(container, "sessionTitle", "知识中心问答");

        const forms = container.querySelectorAll("form");
        expect(forms.length).toBeGreaterThanOrEqual(2);

        await act(async () => {
            forms[0]?.dispatchEvent(new SubmitEvent("submit", { bubbles: true, cancelable: true }));
            await Promise.resolve();
        });

        expect(mocks.openQaSession).toHaveBeenCalledWith({
            contextContentId: null,
            contextContentType: null,
            contextMode: null,
            ownerUserId: 1,
            requestId: null,
            scope: null,
            title: "知识中心问答",
            traceId: null
        });
        expect(container.textContent).toContain("会话 2001");

        setTextareaValue(container, "question", "礼器在哪里出现？");

        await act(async () => {
            forms[1]?.dispatchEvent(new SubmitEvent("submit", { bubbles: true, cancelable: true }));
            await Promise.resolve();
        });

        expect(mocks.askQaQuestion).toHaveBeenCalledWith({
            contextTurnCount: 3,
            operatorId: "portal-user",
            operatorType: "PORTAL",
            requestId: null,
            question: "礼器在哪里出现？",
            sessionId: 2001,
            traceId: null
        });
        expect(container.textContent).toContain("礼器通常出现在礼制、器物和制度相关章节。");
        expect(container.textContent).toContain("礼器条目");
        expect(container.textContent).toContain("礼器的章节位置是什么？");
        expect(container.textContent).toContain("候选数2");

        act(() => {
            root.unmount();
        });
    });
});
