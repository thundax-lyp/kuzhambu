import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import * as classicsContentService from "@/pages/classics/common/classics-content-service";
import { AiCandidateBatchDrawer } from "./ai-candidate-batch-drawer";

const confirmDanger = vi.hoisted(() =>
    vi.fn(({ onConfirm }: { onConfirm: () => void }) => onConfirm())
);
const messageWarning = vi.hoisted(() => vi.fn());
const messageSuccess = vi.hoisted(() => vi.fn());
const messageError = vi.hoisted(() => vi.fn());

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: confirmDanger
    })
}));

vi.mock("@/pages/classics/common/ai-candidate-service", () => ({
    list: vi.fn(),
    apply: vi.fn(),
    reject: vi.fn()
}));

vi.mock("@/pages/classics/common/classics-content-service", () => ({
    applyAiCandidatesBatch: vi.fn(),
    rejectAiCandidatesBatch: vi.fn()
}));

vi.mock("antd", async () => {
    const actual = await vi.importActual<typeof import("antd")>("antd");
    return {
        ...actual,
        App: {
            ...actual.App,
            useApp: () => ({
                message: {
                    warning: messageWarning,
                    success: messageSuccess,
                    error: messageError
                },
                notification: {},
                modal: {}
            })
        }
    };
});

const renderDrawer = (onChanged: () => Promise<void> | void) => {
    render(
        <QueryClientProvider
            client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}
        >
            <AntdApp>
                <AiCandidateBatchDrawer
                    open
                    contentType="SANCAI_ENTRY"
                    contentIds={["3001", "3002"]}
                    capabilities={["summary", "image_analysis"]}
                    contentTitleById={{
                        3001: "天地",
                        3002: "地理"
                    }}
                    canEdit
                    onClose={vi.fn()}
                    onChanged={onChanged}
                />
            </AntdApp>
        </QueryClientProvider>
    );
};

const createListMock = (
    candidatesByContentId: Record<string, Awaited<ReturnType<typeof aiCandidateService.list>>>
) => {
    let fallbackIndex = 0;
    const fallbackOrder = ["3001", "3002"];

    return vi.fn(async (query: Parameters<typeof aiCandidateService.list>[0]) => {
        const request = query as { contentId?: string; queryKey?: unknown[] };
        const fallbackContentId = fallbackOrder[fallbackIndex++] ?? request?.contentId;
        const contentId = String(request.contentId ?? request.queryKey?.at(4) ?? fallbackContentId);

        return candidatesByContentId[contentId] ?? [];
    }) as (
        query: Parameters<typeof aiCandidateService.list>[0]
    ) => Promise<Awaited<ReturnType<typeof aiCandidateService.list>>>;
};

describe("AiCandidateBatchDrawer", () => {
    afterEach(() => {
        cleanup();
        messageWarning.mockReset();
        messageSuccess.mockReset();
        messageError.mockReset();
        confirmDanger.mockReset();
        vi.mocked(aiCandidateService.list).mockReset();
        vi.mocked(classicsContentService.applyAiCandidatesBatch).mockReset();
        vi.mocked(classicsContentService.rejectAiCandidatesBatch).mockReset();
    });

    it("loads pending candidates for selected content and filters by capability", async () => {
        vi.mocked(aiCandidateService.list).mockImplementation(
            createListMock({
                3001: [
                    {
                        candidateId: "7001",
                        contentType: "SANCAI_ENTRY",
                        contentId: "3001",
                        capability: "summary",
                        objectId: null,
                        resultFormat: "TEXT",
                        resultPayload: "摘要候选",
                        status: "PENDING",
                        requestedAt: "2026-07-01T10:00:00.000+08:00"
                    }
                ],
                3002: [
                    {
                        candidateId: "7002",
                        contentType: "SANCAI_ENTRY",
                        contentId: "3002",
                        capability: "image_analysis",
                        objectId: "9002",
                        resultFormat: "TEXT",
                        resultPayload: "图像文本",
                        status: "PENDING",
                        requestedAt: "2026-07-01T10:01:00.000+08:00"
                    },
                    {
                        candidateId: "7003",
                        contentType: "SANCAI_ENTRY",
                        contentId: "3002",
                        capability: "qa",
                        objectId: null,
                        resultFormat: "STRUCTURED",
                        resultPayload: "{}",
                        status: "PENDING",
                        requestedAt: "2026-07-01T10:02:00.000+08:00"
                    }
                ]
            })
        );

        renderDrawer(vi.fn());

        expect(await screen.findByText(/已选内容\s*2\s*个/)).toBeInTheDocument();
        expect(await screen.findByText("天地")).toBeInTheDocument();
        expect(await screen.findByText("地理")).toBeInTheDocument();
        expect(await screen.findByText("summary")).toBeInTheDocument();
        expect(await screen.findByText("image_analysis")).toBeInTheDocument();
        expect(screen.queryByText("qa")).not.toBeInTheDocument();
        expect(await screen.findByText("9002")).toBeInTheDocument();
        expect(screen.getByText(/待处理候选\s*2\s*个/)).toBeInTheDocument();
        expect(aiCandidateService.list).toHaveBeenNthCalledWith(1, {
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            status: "PENDING"
        });
        expect(aiCandidateService.list).toHaveBeenNthCalledWith(2, {
            contentType: "SANCAI_ENTRY",
            contentId: "3002",
            status: "PENDING"
        });
    });

    it("warns when batch apply is blocked by payload validation", async () => {
        const user = userEvent.setup();
        vi.mocked(aiCandidateService.list).mockImplementation(
            createListMock({
                3001: [
                    {
                        candidateId: "7101",
                        contentType: "SANCAI_ENTRY",
                        contentId: "3001",
                        capability: "summary",
                        objectId: null,
                        resultFormat: "TEXT",
                        resultPayload: "",
                        status: "PENDING"
                    }
                ],
                3002: [
                    {
                        candidateId: "7102",
                        contentType: "SANCAI_ENTRY",
                        contentId: "3002",
                        capability: "image_analysis",
                        objectId: "8001",
                        resultFormat: "TEXT",
                        resultPayload: "有效内容",
                        status: "PENDING"
                    }
                ]
            })
        );

        renderDrawer(vi.fn());

        const checkboxes = await screen.findAllByRole("checkbox");
        await user.click(checkboxes[1]);
        await user.click(checkboxes[2]);
        await user.click(screen.getByRole("button", { name: "批量应用" }));

        expect(messageWarning).toHaveBeenCalledWith("请先修正候选内容");
        expect(classicsContentService.applyAiCandidatesBatch).not.toHaveBeenCalled();
    });

    it("submits batch apply and displays failure details", async () => {
        const user = userEvent.setup();
        const onChanged = vi.fn().mockResolvedValue(undefined);

        vi.mocked(aiCandidateService.list).mockImplementation(
            createListMock({
                3001: [
                    {
                        candidateId: "7201",
                        contentType: "SANCAI_ENTRY",
                        contentId: "3001",
                        capability: "summary",
                        resultPayload: "初稿",
                        resultFormat: "TEXT",
                        status: "PENDING"
                    }
                ],
                3002: [
                    {
                        candidateId: "7202",
                        contentType: "SANCAI_ENTRY",
                        contentId: "3002",
                        capability: "summary",
                        resultPayload: "再次提交",
                        resultFormat: "TEXT",
                        status: "PENDING"
                    }
                ]
            })
        );

        vi.mocked(classicsContentService.applyAiCandidatesBatch).mockResolvedValue({
            failureCount: 1,
            failures: [
                {
                    candidateId: "7202",
                    contentId: "3002",
                    contentType: "SANCAI_ENTRY",
                    capability: "summary",
                    failureCode: "UNKNOWN",
                    failureReason: "示例失败"
                }
            ],
            successCount: 1,
            successes: [
                {
                    candidateId: "7201",
                    contentId: "3001",
                    contentType: "SANCAI_ENTRY",
                    resultId: "9001",
                    status: "APPLIED",
                    capability: "summary"
                }
            ]
        });

        vi.mocked(aiCandidateService.list)
            .mockResolvedValueOnce([
                {
                    candidateId: "7201",
                    contentType: "SANCAI_ENTRY",
                    contentId: "3001",
                    capability: "summary",
                    resultPayload: "初稿",
                    resultFormat: "TEXT",
                    status: "PENDING"
                }
            ])
            .mockResolvedValueOnce([
                {
                    candidateId: "7202",
                    contentType: "SANCAI_ENTRY",
                    contentId: "3002",
                    capability: "summary",
                    resultPayload: "再次提交",
                    resultFormat: "TEXT",
                    status: "PENDING"
                }
            ]);

        renderDrawer(onChanged);

        const checkboxes = await screen.findAllByRole("checkbox");
        await user.click(checkboxes[1]);
        await user.click(checkboxes[2]);
        await user.click(screen.getByRole("button", { name: "批量应用" }));

        expect(await screen.findByText("批量候选应用结果：成功 1，失败 1")).toBeInTheDocument();
        expect(screen.getByText("7202 / summary / 示例失败")).toBeInTheDocument();

        expect(classicsContentService.applyAiCandidatesBatch).toHaveBeenNthCalledWith(
            1,
            {
                items: [
                    {
                        candidateId: "7201",
                        contentType: "SANCAI_ENTRY",
                        contentId: "3001",
                        capability: "summary",
                        objectId: undefined,
                        resultFormat: "TEXT",
                        resultPayload: "初稿",
                        changeSummary: "AI 应用：summary"
                    },
                    {
                        candidateId: "7202",
                        contentType: "SANCAI_ENTRY",
                        contentId: "3002",
                        capability: "summary",
                        objectId: undefined,
                        resultFormat: "TEXT",
                        resultPayload: "再次提交",
                        changeSummary: "AI 应用：summary"
                    }
                ]
            },
            expect.any(Object)
        );
        expect(onChanged).toHaveBeenCalledTimes(1);
    });

    it("confirms batch reject and submits objectId field", async () => {
        const user = userEvent.setup();
        const onChanged = vi.fn().mockResolvedValue(undefined);

        vi.mocked(aiCandidateService.list).mockImplementation(
            createListMock({
                3001: [
                    {
                        candidateId: "7301",
                        contentType: "SANCAI_ENTRY",
                        contentId: "3001",
                        capability: "summary",
                        objectId: "6001",
                        resultFormat: "TEXT",
                        resultPayload: "待拒绝",
                        status: "PENDING"
                    }
                ]
            })
        );

        vi.mocked(classicsContentService.rejectAiCandidatesBatch).mockResolvedValue({
            failureCount: 0,
            failures: [],
            successCount: 1,
            successes: [
                {
                    candidateId: "7301",
                    contentId: "3001",
                    contentType: "SANCAI_ENTRY",
                    capability: "summary",
                    resultId: "3001",
                    status: "REJECTED"
                }
            ]
        });

        render(
            <QueryClientProvider
                client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}
            >
                <AntdApp>
                    <AiCandidateBatchDrawer
                        open
                        contentType="SANCAI_ENTRY"
                        contentIds={["3001"]}
                        capabilities={["summary"]}
                        contentTitleById={{
                            3001: "天地"
                        }}
                        canEdit
                        onClose={vi.fn()}
                        onChanged={onChanged}
                    />
                </AntdApp>
            </QueryClientProvider>
        );

        const checkboxes = await screen.findAllByRole("checkbox");
        const checkbox = checkboxes[1];
        await user.click(checkbox);
        await user.click(screen.getByRole("button", { name: "批量拒绝" }));

        expect(confirmDanger).toHaveBeenCalledTimes(1);
        expect(classicsContentService.rejectAiCandidatesBatch).toHaveBeenNthCalledWith(
            1,
            {
                errorType: "USER_REJECTED",
                errorMessage: "用户已批量拒绝该 AI 候选",
                items: [
                    {
                        candidateId: "7301",
                        contentType: "SANCAI_ENTRY",
                        contentId: "3001",
                        capability: "summary",
                        objectId: "6001"
                    }
                ]
            },
            expect.any(Object)
        );
        expect(await screen.findByText("批量候选拒绝结果：成功 1，失败 0")).toBeInTheDocument();
        expect(onChanged).toHaveBeenCalledTimes(1);
    });
});
