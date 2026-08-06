import { AdminQueryProvider } from "@/query/query-client";
import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { QaPage } from "./qa-page";

const mocks = vi.hoisted(() => ({
    createQaSession: vi.fn(async () => ({
        contextMode: "GENERAL",
        id: "7001",
        openedAt: 1700000000000,
        title: "知识中心问答"
    })),
    createQaSessionExport: vi.fn(async () => ({
        exportStatus: "SUCCEEDED",
        id: "7001",
        filename: "discovery-qa-session-7001.csv",
        sessionId: "7001"
    })),
    createQaChatCompletion: vi.fn(),
    createQaChatCompletionStream: vi.fn(async ({ onDelta }) => {
        onDelta?.("礼学可作为礼制相关内容的检索扩展。");
        return {
            answerStatus: "SUCCEEDED",
            choices: [
                {
                    message: {
                        content: "礼学可作为礼制相关内容的检索扩展。"
                    }
                }
            ],
            id: "chat-7001",
            sessionId: "7001",
            sources: [
                {
                    contentId: "1001",
                    contentType: "SANCAI_ENTRY",
                    knowledgeBase: "SANCAI_ENTRY",
                    sourceId: "SANCAI_ENTRY:1001",
                    titleSnapshot: "礼制条目"
                }
            ]
        };
    }),
    getQaSession: vi.fn(async () => ({
        messages: [] as Array<{
            content: string;
            id: string;
            messageStatus: string;
            role: string;
            sessionId: string;
        }>,
        id: "7001",
        openedAt: 1700000000000,
        title: "知识中心问答"
    })),
    deleteQaSession: vi.fn(async () => undefined),
    pageQaSessions: vi.fn(async () => ({
        count: 0,
        pageNo: 1,
        pageSize: 20,
        records: [] as Array<{ id: string; openedAt: number; title: string }>,
        totalPage: 0
    }))
}));
const currentUserMocks = vi.hoisted(() => ({
    getCurrentUserInfo: vi.fn(async () => ({ id: "9001", loginName: "qa-user" }))
}));

vi.mock("./qa-service", () => mocks);
vi.mock("@/service/current-user-service", () => currentUserMocks);

const renderPage = (initialEntry = "/discovery/qa") => {
    return render(
        <AdminQueryProvider>
            <AntdApp>
                <MemoryRouter initialEntries={[initialEntry]}>
                    <Routes>
                        <Route path="/discovery/qa" element={<QaPage />} />
                    </Routes>
                </MemoryRouter>
            </AntdApp>
        </AdminQueryProvider>
    );
};

describe("QaPage", () => {
    beforeEach(() => {
        Object.values(mocks).forEach((mock) => mock.mockClear());
        currentUserMocks.getCurrentUserInfo.mockClear();
        mocks.createQaSession.mockImplementation(async () => ({
            contextMode: "GENERAL",
            id: "7001",
            openedAt: 1700000000000,
            title: "知识中心问答"
        }));
    });

    afterEach(() => {
        cleanup();
        vi.restoreAllMocks();
    });

    it("renders intelligent qa shell", async () => {
        renderPage();

        expect(screen.getByRole("heading", { name: "知识助手" })).toBeInTheDocument();
        expect(screen.getByLabelText("问题")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "发送问题" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "新建对话" })).toBeInTheDocument();
        expect(screen.queryByText("全库问答")).not.toBeInTheDocument();
        expect(screen.queryByLabelText("上下文模式")).not.toBeInTheDocument();
        expect(screen.queryByLabelText("上下文类型")).not.toBeInTheDocument();
        expect(screen.queryByLabelText("上下文 ID")).not.toBeInTheDocument();
        expect(screen.queryByLabelText("请求 ID")).not.toBeInTheDocument();
        expect(screen.queryByLabelText("Trace ID")).not.toBeInTheDocument();
        await waitFor(() => {
            expect(mocks.pageQaSessions).toHaveBeenCalledWith({
                ownerUserId: "9001",
                pageNo: 1,
                pageSize: 20,
                scope: "PORTAL"
            });
        });
    });

    it("opens session and sends question", async () => {
        const user = userEvent.setup();
        renderPage();

        await user.type(screen.getByLabelText("问题"), "礼学和礼制有什么关系？");
        await user.click(screen.getByRole("button", { name: "发送问题" }));

        await waitFor(() => {
            expect(mocks.createQaSession).toHaveBeenCalledWith(
                expect.objectContaining({
                    contextContentId: null,
                    contextContentType: null,
                    contextMode: "GENERAL",
                    ownerUserId: "9001",
                    requestId: null,
                    scope: "PORTAL",
                    title: "礼学和礼制有什么关系？",
                    traceId: null
                }),
                expect.anything()
            );
        });
        await waitFor(() => {
            expect(mocks.createQaChatCompletionStream).toHaveBeenCalledWith(
                expect.objectContaining({
                    command: {
                        messages: [{ content: "礼学和礼制有什么关系？", role: "user" }],
                        metadata: {
                            contextContentId: null,
                            contextContentType: null,
                            contextMode: "GENERAL",
                            sessionId: "7001"
                        },
                        model: "kuzhambu-qa",
                        requestId: null,
                        sessionId: "7001",
                        stream: true,
                        traceId: null
                    },
                    onDelta: expect.any(Function)
                }),
                expect.anything()
            );
        });
        expect(await screen.findByText("礼学和礼制有什么关系？")).toBeInTheDocument();
        expect(screen.getByText("礼学可作为礼制相关内容的检索扩展。")).toBeInTheDocument();
        expect(screen.getByText("礼制条目")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "导出对话" }));
        await waitFor(() => {
            expect(mocks.createQaSessionExport).toHaveBeenCalledWith(
                {
                    format: "CSV",
                    ownerUserId: "9001",
                    sessionId: "7001"
                },
                expect.anything()
            );
        });
        expect(
            await screen.findByText("导出完成：discovery-qa-session-7001.csv")
        ).toBeInTheDocument();
    });

    it("starts a local new conversation draft explicitly", async () => {
        const user = userEvent.setup();
        renderPage();

        await user.click(screen.getByRole("button", { name: "新建对话" }));

        expect(mocks.createQaSession).not.toHaveBeenCalled();
        expect(mocks.createQaChatCompletionStream).not.toHaveBeenCalled();
    });

    it("renders messages from an existing session detail", async () => {
        mocks.pageQaSessions.mockResolvedValueOnce({
            count: 1,
            pageNo: 1,
            pageSize: 20,
            records: [
                {
                    id: "7001",
                    openedAt: 1700000000000,
                    title: "既有对话"
                }
            ],
            totalPage: 1
        });
        mocks.getQaSession.mockResolvedValueOnce({
            messages: [
                {
                    content: "礼学是什么？",
                    id: "8001",
                    messageStatus: "SENT",
                    role: "USER",
                    sessionId: "7001"
                },
                {
                    content: "礼学是礼制相关的学问。",
                    id: "8002",
                    messageStatus: "SUCCEEDED",
                    role: "ASSISTANT",
                    sessionId: "7001"
                }
            ],
            id: "7001",
            openedAt: 1700000000000,
            title: "既有对话"
        });
        const user = userEvent.setup();
        renderPage();

        await user.click(await screen.findByRole("button", { name: "既有对话" }));

        expect(await screen.findByText("礼学是什么？")).toBeInTheDocument();
        expect(screen.getByText("礼学是礼制相关的学问。")).toBeInTheDocument();
        expect(mocks.getQaSession).toHaveBeenCalledWith({
            ownerUserId: "9001",
            sessionId: "7001"
        });

        await user.type(screen.getByLabelText("问题"), "礼学有哪些代表人物？");
        await user.click(screen.getByRole("button", { name: "发送问题" }));

        expect(await screen.findByText("礼学有哪些代表人物？")).toBeInTheDocument();
        expect(screen.getByText("礼学是什么？")).toBeInTheDocument();
        expect(screen.getByText("礼学是礼制相关的学问。")).toBeInTheDocument();
    });

    it("truncates first question as automatic conversation title", async () => {
        const user = userEvent.setup();
        renderPage();

        await user.type(
            screen.getByLabelText("问题"),
            "这是一段很长很长的提问，用来验证对话标题会被截断显示"
        );
        await user.click(screen.getByRole("button", { name: "发送问题" }));

        await waitFor(() => {
            expect(mocks.createQaSession).toHaveBeenCalledWith(
                expect.objectContaining({
                    title: "这是一段很长很长的提问，用来验证对话标题会被截断..."
                }),
                expect.anything()
            );
        });
    });

    it("deletes a conversation from the session list", async () => {
        mocks.pageQaSessions.mockResolvedValueOnce({
            count: 1,
            pageNo: 1,
            pageSize: 20,
            records: [
                {
                    id: "7001",
                    openedAt: 1700000000000,
                    title: "礼学和礼制有什么关系？"
                }
            ],
            totalPage: 1
        });
        const user = userEvent.setup();
        renderPage();

        await user.click(await screen.findByTestId("discovery-qa-delete-session-button"));

        const confirmDialog = await screen.findByRole("dialog");
        expect(screen.getByText("确认删除「礼学和礼制有什么关系？」？")).toBeInTheDocument();
        await user.click(within(confirmDialog).getByRole("button", { name: /删\s*除/ }));

        await waitFor(() => {
            expect(mocks.deleteQaSession).toHaveBeenCalledWith(
                {
                    ownerUserId: "9001",
                    sessionId: "7001"
                },
                expect.anything()
            );
        });
    });

    it("shows visible error when first message cannot open a session", async () => {
        mocks.createQaSession.mockRejectedValueOnce(new Error("创建会话失败"));
        const user = userEvent.setup();
        renderPage();

        await user.type(screen.getByLabelText("问题"), "礼学和礼制有什么关系？");
        await user.click(screen.getByRole("button", { name: "发送问题" }));

        expect(await screen.findByText("创建会话失败")).toBeInTheDocument();
        expect(screen.getByLabelText("问题")).toHaveValue("礼学和礼制有什么关系？");
        expect(mocks.createQaChatCompletionStream).not.toHaveBeenCalled();
    });

    it("aborts the active stream when starting a new conversation", async () => {
        let streamSignal: AbortSignal | undefined;
        mocks.createQaChatCompletionStream.mockImplementationOnce(({ signal }) => {
            streamSignal = signal;
            return new Promise((_resolve, reject) => {
                signal?.addEventListener("abort", () =>
                    reject(new DOMException("Aborted", "AbortError"))
                );
            });
        });
        const user = userEvent.setup();
        renderPage();

        await user.type(screen.getByLabelText("问题"), "持续生成回答");
        await user.click(screen.getByRole("button", { name: "发送问题" }));
        await waitFor(() => expect(streamSignal).toBeDefined());
        await user.click(screen.getByRole("button", { name: "新建对话" }));

        expect(streamSignal?.aborted).toBe(true);
    });

    it("ignores url context filters and keeps full-library qa", async () => {
        renderPage(
            "/discovery/qa?contextContentType=WANGQI_DOCUMENT&contextContentId=3001&contextMode=SINGLE_DOCUMENT&title=%E7%8E%8B%E5%9C%BB%E5%AE%98%E5%88%B6"
        );

        expect(screen.getByRole("heading", { name: "知识助手" })).toBeInTheDocument();
        expect(screen.queryByDisplayValue("王圻官制")).not.toBeInTheDocument();
        expect(screen.queryByDisplayValue("3001")).not.toBeInTheDocument();
        expect(screen.queryByLabelText("上下文模式")).not.toBeInTheDocument();
    });
});
