import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import * as service from "./qa-admin-service";
import { QaAdminPage } from "./qa-admin-page";

vi.mock("./qa-admin-service", () => ({
    getQaSessionDetail: vi.fn(async () => ({
        sessionId: 2001,
        title: "礼器问答",
        status: "OPEN",
        scope: "portal",
        contextMode: "QA",
        contextContentType: "SANCAI_ENTRY",
        openedAt: 1700000000000,
        messages: [
            {
                messageId: 4001,
                sessionId: 2001,
                role: "USER",
                content: "礼器在哪里出现？",
                messageStatus: "SENT",
                contextTurnCount: 3,
                sentAt: "2026-01-01 10:00:00"
            }
        ]
    })),
    getQaTrace: vi.fn(async () => ({
        traceId: 9001,
        rawQuestion: "礼器在哪里出现？",
        rewrittenQuestion: "礼器的章节位置是什么？",
        candidateCount: 2,
        scope: "portal",
        filtersJson: '{"contentType":"SANCAI_ENTRY"}',
        expandedTermsJson: '["礼器"]',
        linkedEntitiesJson: '["器物"]',
        contextSnapshot: '{"sessionId":2001}',
        retrievedAt: "2026-01-01 10:00:00"
    })),
    listQaSources: vi.fn(async () => [
        {
            sourceId: 5001,
            knowledgeBase: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: 1001,
            titleSnapshot: "礼器条目",
            locationLabel: "卷一",
            snippet: "礼器，礼之所用也。",
            sourceRank: 1,
            score: 0.9123,
            sourceStatus: "ACTIVE"
        }
    ])
}));

describe("QaAdminPage", () => {
    const mockedService = vi.mocked(service);

    beforeEach(() => {
        queryClient.clear();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.restoreAllMocks();
    });

    it("loads session, sources and trace details", async () => {
        const user = userEvent.setup();
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <QaAdminPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(screen.getByRole("button", { name: "加载会话" }));
        expect(await screen.findByText("礼器问答")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "加载来源" }));
        expect(await screen.findByText("礼器条目")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "加载轨迹" }));
        expect(await screen.findByText("礼器的章节位置是什么？")).toBeInTheDocument();

        await waitFor(() => {
            expect(mockedService.getQaSessionDetail).toHaveBeenCalled();
            expect(mockedService.listQaSources).toHaveBeenCalled();
            expect(mockedService.getQaTrace).toHaveBeenCalled();
        });
        expect(mockedService.getQaSessionDetail.mock.calls.at(-1)?.[0]).toEqual({
            sessionId: 2001
        });
        expect(mockedService.listQaSources.mock.calls.at(-1)?.[0]).toEqual({ messageId: 4001 });
        expect(mockedService.getQaTrace.mock.calls.at(-1)?.[0]).toEqual({ traceId: 9001 });
    });
});
