import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { queryClient } from "@/query/query-client";
import { QaPage } from "./qa-page";

const mocks = vi.hoisted(() => ({
    createQaSession: vi.fn(async () => ({
        contextMode: "GENERAL",
        openedAt: 1700000000000,
        sessionId: "7001",
        title: "知识中心问答"
    })),
    createQaSessionExport: vi.fn(async () => ({
        exportStatus: "SUCCEEDED",
        filename: "discovery-qa-session-7001.csv",
        sessionId: "7001"
    })),
    createQaChatCompletion: vi.fn(async () => ({
        answerStatus: "SUCCEEDED",
        choices: [
            {
                message: {
                    content: "礼学可作为礼制相关内容的检索扩展。"
                }
            }
        ],
        sessionId: "7001",
        sources: [
            {
                contentId: 1001,
                contentType: "SANCAI_ENTRY",
                knowledgeBase: "SANCAI_ENTRY",
                sourceId: "SANCAI_ENTRY:1001",
                titleSnapshot: "礼制条目"
            }
        ]
    })),
    getQaSession: vi.fn(async () => ({
        openedAt: 1700000000000,
        sessionId: "7001",
        title: "知识中心问答"
    })),
    pageQaSessions: vi.fn(async () => ({
        items: [],
        pageNo: 1,
        pageSize: 20,
        total: 0
    }))
}));

vi.mock("./qa-service", () => mocks);

const renderPage = (initialEntry = "/discovery/qa") => {
    return render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <MemoryRouter initialEntries={[initialEntry]}>
                    <Routes>
                        <Route path="/discovery/qa" element={<QaPage />} />
                    </Routes>
                </MemoryRouter>
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("QaPage", () => {
    beforeEach(() => {
        queryClient.clear();
        Object.values(mocks).forEach((mock) => mock.mockClear());
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.restoreAllMocks();
    });

    it("renders intelligent qa shell", async () => {
        renderPage();

        expect(screen.getByRole("heading", { name: "智能问答" })).toBeInTheDocument();
        expect(screen.getByLabelText("问题")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "发送问题" })).toBeInTheDocument();
        await waitFor(() => {
            expect(mocks.pageQaSessions).toHaveBeenCalledWith({
                ownerUserId: 1001,
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
                    contextMode: "GENERAL",
                    ownerUserId: 1001,
                    scope: "PORTAL",
                    title: "知识中心问答"
                }),
                expect.anything()
            );
        });
        await waitFor(() => {
            expect(mocks.createQaChatCompletion).toHaveBeenCalledWith(
                expect.objectContaining({
                    messages: [{ content: "礼学和礼制有什么关系？", role: "user" }],
                    model: "kuzhambu-qa",
                    sessionId: "7001",
                    stream: false
                }),
                expect.anything()
            );
        });
        expect(await screen.findByText("礼学和礼制有什么关系？")).toBeInTheDocument();
        expect(screen.getByText("礼学可作为礼制相关内容的检索扩展。")).toBeInTheDocument();
        expect(screen.getByText("礼制条目")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "导出 CSV" }));
        await waitFor(() => {
            expect(mocks.createQaSessionExport).toHaveBeenCalledWith(
                {
                    format: "CSV",
                    ownerUserId: 1001,
                    sessionId: "7001"
                },
                expect.anything()
            );
        });
        expect(
            await screen.findByText("导出成功：discovery-qa-session-7001.csv")
        ).toBeInTheDocument();
    });

    it("locks Wangqi single document context from url", async () => {
        renderPage(
            "/discovery/qa?contextContentType=WANGQI_DOCUMENT&contextContentId=3001&contextMode=SINGLE_DOCUMENT&title=%E7%8E%8B%E5%9C%BB%E5%AE%98%E5%88%B6"
        );

        expect(screen.getByLabelText("上下文模式")).toBeDisabled();
        expect(screen.getByLabelText("上下文类型")).toBeDisabled();
        expect(screen.getByLabelText("上下文 ID")).toBeDisabled();
        expect(screen.getByDisplayValue("王圻官制")).toBeInTheDocument();
        expect(screen.getByDisplayValue("3001")).toBeInTheDocument();
    });
});
