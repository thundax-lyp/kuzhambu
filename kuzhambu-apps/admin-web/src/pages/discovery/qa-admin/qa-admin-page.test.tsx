import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { QaAdminPage } from "./qa-admin-page";

const mocks = vi.hoisted(() => ({
    getKnowledgeHealth: vi.fn(),
    getQaSession: vi.fn(),
    getQaTrace: vi.fn(),
    listQaSources: vi.fn(),
    pageKnowledgeSyncItems: vi.fn(),
    rebuildKnowledge: vi.fn(),
    createKnowledgeSync: vi.fn()
}));

vi.mock("./qa-admin-service", () => ({
    getKnowledgeHealth: mocks.getKnowledgeHealth,
    getQaSession: mocks.getQaSession,
    getQaSessionDetail: mocks.getQaSession,
    getQaTrace: mocks.getQaTrace,
    listQaSources: mocks.listQaSources,
    pageKnowledgeSyncItems: mocks.pageKnowledgeSyncItems,
    rebuildKnowledge: mocks.rebuildKnowledge,
    createKnowledgeSync: mocks.createKnowledgeSync
}));

const renderPage = () => {
    return render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <QaAdminPage />
            </AntdApp>
        </QueryClientProvider>
    );
};

const findButtonByNormalizedText = (text: string) => {
    const button = screen
        .getAllByRole("button")
        .find((element) => element.textContent?.replace(/\s+/gu, "") === text);
    expect(button).toBeDefined();
    return button as HTMLButtonElement;
};

describe("QaAdminPage", () => {
    beforeEach(() => {
        queryClient.clear();
        mocks.getKnowledgeHealth.mockResolvedValue({
            checkedAt: 1700000000000,
            knowledgeBaseName: "kuzhambu-qa",
            provider: "FASTGPT",
            status: "AVAILABLE"
        });
        mocks.getQaSession.mockResolvedValue({
            sessionId: 2001,
            title: "礼器问答",
            status: "OPEN",
            scope: "PORTAL",
            contextMode: "QA",
            contextContentType: "SANCAI_ENTRY",
            openedAt: 1700000000000,
            messages: [
                {
                    messageId: 4001,
                    role: "USER",
                    content: "礼器在哪里出现？",
                    messageStatus: "SUCCEEDED"
                }
            ]
        });
        mocks.getQaTrace.mockResolvedValue({
            traceId: 9001,
            provider: "FASTGPT",
            externalKnowledgeBaseId: "kb-1",
            externalKnowledgeItemIds: "item-1,item-2",
            externalChatId: "chat-1",
            providerRequestId: "req-1",
            latencyMs: 123,
            raw: '{"answer":"礼器答案"}',
            retrievedAt: "2026-01-01 10:00:00"
        });
        mocks.listQaSources.mockResolvedValue([
            {
                contentId: 1001,
                contentType: "SANCAI_ENTRY",
                knowledgeBase: "kuzhambu-qa",
                score: 0.91,
                sourceId: 5001,
                sourceRank: 1,
                sourceStatus: "AVAILABLE",
                snippet: "礼器，礼之所用也。",
                titleSnapshot: "礼器条目"
            }
        ]);
        mocks.pageKnowledgeSyncItems.mockResolvedValue({
            pageNo: 1,
            pageSize: 10,
            count: 1,
            totalCount: 1,
            totalPage: 1,
            records: [
                {
                    sourceId: "SANCAI_ENTRY:1001",
                    contentType: "SANCAI_ENTRY",
                    contentId: 1001,
                    knowledgeBaseName: "kuzhambu-qa",
                    currentVersionNo: 2,
                    knowledgeRevision: "rev-2",
                    syncStatus: "SYNCED",
                    syncedAt: 1700000000000,
                    updatedAt: 1700000001000
                }
            ]
        });
        mocks.rebuildKnowledge.mockResolvedValue(3);
        mocks.createKnowledgeSync.mockResolvedValue({
            sourceId: "SANCAI_ENTRY:1001",
            contentType: "SANCAI_ENTRY",
            contentId: 1001,
            syncStatus: "SYNCED"
        });
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.restoreAllMocks();
    });

    it("renders health block and rebuild action", async () => {
        const user = userEvent.setup();
        renderPage();

        expect(await screen.findByRole("heading", { name: "问答运维台" })).toBeInTheDocument();
        expect(await screen.findByText("kuzhambu-qa")).toBeInTheDocument();
        expect(screen.getByText("AVAILABLE")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "重建知识库" }));

        await waitFor(() => {
            expect(mocks.rebuildKnowledge.mock.calls[0]?.[0]).toEqual({});
        });
        expect(await screen.findByText("3")).toBeInTheDocument();
    }, 30000);

    it("renders sync table and applies contentType and syncStatus filters", async () => {
        const user = userEvent.setup();
        renderPage();

        await user.clear(screen.getByLabelText("同步状态"));
        await user.type(screen.getByLabelText("同步状态"), "SYNCED");
        await user.click(screen.getByRole("button", { name: "查询同步" }));

        await waitFor(() => {
            expect(mocks.pageKnowledgeSyncItems.mock.calls.at(-1)?.[0]).toEqual({
                contentType: "SANCAI_ENTRY",
                pageNo: 1,
                pageSize: 10,
                syncStatus: "SYNCED"
            });
        });
        expect(await screen.findByText("SANCAI_ENTRY:1001")).toBeInTheDocument();
        expect(screen.getByText(/rev-2/u)).toBeInTheDocument();
    }, 30000);

    it("supports row sync action", async () => {
        const user = userEvent.setup();
        renderPage();

        await user.click(screen.getByRole("button", { name: "查询同步" }));
        await screen.findByText("SANCAI_ENTRY:1001");
        await user.click(findButtonByNormalizedText("同步"));

        await waitFor(() => {
            expect(mocks.createKnowledgeSync.mock.calls[0]?.[0]).toEqual({
                contentId: 1001,
                contentType: "SANCAI_ENTRY",
                currentVersionNo: 2
            });
        });
    }, 30000);

    it("loads session sources and provider trace with formatted raw json", async () => {
        const user = userEvent.setup();
        renderPage();

        await user.click(screen.getByRole("button", { name: "加载会话" }));
        expect(await screen.findByText("礼器问答")).toBeInTheDocument();
        expect(screen.getByText("礼器在哪里出现？")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "加载来源" }));
        expect(await screen.findByText("礼器条目")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "加载轨迹" }));
        expect(await screen.findByText("kb-1")).toBeInTheDocument();
        expect(screen.getByText(/"answer": "礼器答案"/u)).toBeInTheDocument();
    }, 30000);
});
