import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
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
        messages: []
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
    listQaSources: vi.fn(async () => [])
}));

describe("QaAdminPage", () => {
    beforeEach(() => {
        queryClient.clear();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.restoreAllMocks();
    });

    it("renders page shell", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <QaAdminPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "问答调试台" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "加载会话" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "加载来源" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "加载轨迹" })).toBeInTheDocument();
    }, 30000);
});
