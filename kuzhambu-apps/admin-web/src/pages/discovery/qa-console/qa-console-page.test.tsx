import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { QaConsolePage } from "./qa-console-page";

const mocks = vi.hoisted(() => ({
    createQaSessionExport: vi.fn(),
    deleteQaSession: vi.fn(),
    getKnowledgeHealth: vi.fn(),
    getQaSession: vi.fn(),
    getQaTrace: vi.fn(),
    pageQaSessions: vi.fn(),
    pageKnowledgeSyncItems: vi.fn(),
    rebuildKnowledge: vi.fn(),
    createKnowledgeSync: vi.fn()
}));

vi.mock("./qa-console-service", () => ({
    createQaSessionExport: mocks.createQaSessionExport,
    deleteQaSession: mocks.deleteQaSession,
    getKnowledgeHealth: mocks.getKnowledgeHealth,
    getQaSession: mocks.getQaSession,
    getQaSessionDetail: mocks.getQaSession,
    getQaTrace: mocks.getQaTrace,
    pageQaSessions: mocks.pageQaSessions,
    pageKnowledgeSyncItems: mocks.pageKnowledgeSyncItems,
    rebuildKnowledge: mocks.rebuildKnowledge,
    createKnowledgeSync: mocks.createKnowledgeSync
}));

const createTestQueryClient = () =>
    new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

const renderPage = () => {
    const testQueryClient = createTestQueryClient();
    return render(
        <QueryClientProvider client={testQueryClient}>
            <AntdApp>
                <QaConsolePage />
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

const switchPanel = async (user: ReturnType<typeof userEvent.setup>, panelName: string) => {
    await user.click(screen.getByText(panelName));
};

describe("QaConsolePage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mocks.getKnowledgeHealth.mockResolvedValue({
            checkedAt: 1700000000000,
            knowledgeBaseName: "kuzhambu-qa",
            provider: "FASTGPT",
            status: "AVAILABLE"
        });
        mocks.getQaSession.mockResolvedValue({
            ownerUserId: 1001,
            sessionId: "2001",
            title: "礼器问答",
            status: "OPEN",
            scope: "PORTAL",
            contextMode: "QA",
            contextContentType: "SANCAI_ENTRY",
            openedAt: 1700000000000,
            messages: [
                {
                    messageId: "4001",
                    role: "USER",
                    content: "礼器在哪里出现？",
                    messageStatus: "SUCCEEDED"
                }
            ]
        });
        mocks.pageQaSessions.mockResolvedValue({
            pageNo: 1,
            pageSize: 10,
            count: 1,
            totalCount: 1,
            totalPage: 1,
            records: [
                {
                    ownerUserId: 1001,
                    sessionId: "2001",
                    title: "礼器问答",
                    status: "OPEN",
                    scope: "PORTAL",
                    contextMode: "QA",
                    contextContentType: "SANCAI_ENTRY",
                    openedAt: 1700000000000
                }
            ]
        });
        mocks.getQaTrace.mockResolvedValue({
            aiCallId: 9101,
            aiErrorMessage: null,
            aiErrorType: null,
            aiStatus: "SUCCEEDED",
            traceId: "9001",
            provider: "FASTGPT",
            externalKnowledgeBaseId: "kb-1",
            externalKnowledgeItemIds: "item-1,item-2",
            externalChatId: "chat-1",
            providerRequestId: "req-1",
            latencyMs: 123,
            raw: '{"answer":"礼器答案"}',
            retrievedAt: "2026-01-01 10:00:00"
        });
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
                    title: "黄帝",
                    knowledgeBaseName: "kuzhambu-qa",
                    currentVersionNo: 2,
                    knowledgeRevision: "rev-2",
                    syncStatus: "SUCCEEDED",
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
            title: "黄帝",
            syncStatus: "SUCCEEDED"
        });
        mocks.deleteQaSession.mockResolvedValue(undefined);
        mocks.createQaSessionExport.mockResolvedValue({
            exportId: 7001,
            exportStatus: "SUCCEEDED",
            filename: "discovery-qa-session-2001-7001.csv",
            sessionId: "2001",
            storageObjectId: 8001
        });
    });

    afterEach(() => {
        cleanup();
        vi.restoreAllMocks();
    });

    it("renders health block and rebuild action", async () => {
        const user = userEvent.setup();
        renderPage();

        expect(await screen.findByRole("heading", { name: "问答运维" })).toBeInTheDocument();
        expect(await screen.findByText("kuzhambu-qa")).toBeInTheDocument();
        expect(screen.getByText("AVAILABLE")).toBeInTheDocument();

        await switchPanel(user, "知识文档");
        await user.click(screen.getByRole("button", { name: "全部同步" }));

        await waitFor(() => {
            expect(mocks.rebuildKnowledge.mock.calls[0]?.[0]).toEqual({});
        });
    }, 30000);

    it("renders sync table and applies contentType and syncStatus filters", async () => {
        const user = userEvent.setup();
        renderPage();

        await switchPanel(user, "知识文档");
        expect(screen.getByRole("combobox", { name: "内容类型" })).toBeInTheDocument();
        expect(screen.getByRole("combobox", { name: "同步状态" })).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: /查\s*询/u }));

        await waitFor(() => {
            expect(mocks.pageKnowledgeSyncItems.mock.calls.at(-1)?.[0]).toEqual({
                contentType: "SANCAI_ENTRY",
                pageNo: 1,
                pageSize: 10,
                syncStatus: null
            });
        });
        expect(await screen.findByText("黄帝")).toBeInTheDocument();
        expect(screen.getAllByText("三才图会").length).toBeGreaterThan(1);
        expect(screen.getByText("成功")).toBeInTheDocument();
        expect(screen.getAllByText("2023-11-15").length).toBeGreaterThan(0);
    }, 30000);

    it("supports row sync action", async () => {
        renderPage();

        fireEvent.click(screen.getByText("知识文档"));
        fireEvent.click(screen.getByRole("button", { name: /查\s*询/u }));
        await screen.findByText("黄帝");
        fireEvent.click(findButtonByNormalizedText("同步"));

        await waitFor(() => {
            expect(mocks.createKnowledgeSync.mock.calls[0]?.[0]).toEqual({
                contentId: 1001,
                contentType: "SANCAI_ENTRY",
                currentVersionNo: 2
            });
        });
    }, 30000);

    it("loads session table and opens details drawer", async () => {
        const user = userEvent.setup();
        renderPage();

        await switchPanel(user, "会话管理");
        await user.click(screen.getByRole("button", { name: /查\s*询/u }));
        await waitFor(() => {
            expect(mocks.pageQaSessions.mock.calls.at(-1)?.[0]).toEqual({
                openedAtEnd: null,
                openedAtStart: null,
                pageNo: 1,
                pageSize: 10,
                title: null
            });
        });
        expect(await screen.findByText("礼器问答")).toBeInTheDocument();
        fireEvent.click(screen.getByTestId("discovery-qa-console-qa-console-view-session-button"));
        await waitFor(() => {
            expect(mocks.getQaSession.mock.calls[0]?.[0]).toEqual({ sessionId: "2001" });
        });
        expect(await screen.findByText("礼器在哪里出现？")).toBeInTheDocument();
    }, 30000);

    it("links diagnostics to FastGPT", async () => {
        const user = userEvent.setup();
        renderPage();
        await switchPanel(user, "问答诊断");

        expect(screen.getByText("知识条目、分段、召回配置以 FastGPT 为准。")).toBeInTheDocument();
        expect(screen.getByRole("link", { name: "FastGPT 控制台" })).toHaveAttribute(
            "href",
            "http://localhost:13000"
        );
    }, 30000);

    it("deletes session from table action", async () => {
        renderPage();

        fireEvent.click(screen.getByText("会话管理"));
        fireEvent.click(screen.getByRole("button", { name: /查\s*询/u }));
        await waitFor(() => {
            expect(mocks.pageQaSessions.mock.calls.at(-1)?.[0]).toEqual({
                openedAtEnd: null,
                openedAtStart: null,
                pageNo: 1,
                pageSize: 10,
                title: null
            });
        });
        expect(await screen.findByText("礼器问答")).toBeInTheDocument();

        fireEvent.click(
            screen.getByTestId("discovery-qa-console-qa-console-delete-session-button")
        );
        await waitFor(() => {
            expect(mocks.deleteQaSession.mock.calls[0]?.[0]).toEqual({
                requesterUserId: 1001,
                sessionId: "2001"
            });
        });
        expect(screen.getByText("会话 2001 已删除")).toBeInTheDocument();
    }, 30000);
});
