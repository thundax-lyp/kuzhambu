import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import * as service from "./taxonomy-service";
import { TaxonomyPage } from "./taxonomy-page";

const confirmDangerMock = vi.hoisted(() =>
    vi.fn(({ onConfirm }: { onConfirm: () => unknown }) => onConfirm())
);

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({ danger: confirmDangerMock })
}));

vi.mock("./taxonomy-service", () => ({
    pageCategories: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    pageTags: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 3,
        totalPage: 1,
        count: 3,
        records: [
            {
                id: "1001",
                name: "礼制",
                categoryId: "11",
                categoryName: "礼学",
                status: "ENABLED",
                source: "MANUAL",
                reviewStatus: "APPROVED",
                contentRefCount: 2
            },
            {
                id: "1002",
                name: "祭祀",
                categoryId: "11",
                categoryName: "礼学",
                status: "ENABLED",
                source: "MANUAL",
                reviewStatus: "APPROVED",
                contentRefCount: 3
            },
            {
                id: "1003",
                name: "礼典",
                categoryId: "11",
                categoryName: "礼学",
                status: "ENABLED",
                source: "AI_EXTRACTED",
                reviewStatus: "APPROVED",
                contentRefCount: 1
            }
        ]
    })),
    pagePendingTags: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    getTagDetail: vi.fn(async () => null),
    pageSynonyms: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    previewTagMergeImpact: vi.fn(async () => null),
    applyTagMerge: vi.fn(async () => true),
    previewTagBatchMergeImpact: vi.fn(async () => ({
        sourceTags: [
            { id: "1001", name: "礼制" },
            { id: "1003", name: "礼典" }
        ],
        targetTag: { id: "1002", name: "祭祀" },
        aliasesToMerge: [{ id: "2001", name: "礼法", source: "MANUAL" }],
        impactedContentRefs: [
            {
                id: "3001",
                contentTitle: "周礼",
                contentType: "CLASSICS",
                source: "MANUAL"
            }
        ],
        pendingReviewCount: 0,
        governedRecordCount: 2
    })),
    applyTagBatchMerge: vi.fn(async () => true),
    deprecateTag: vi.fn(async () => true),
    deprecateBatchTags: vi.fn(async () => true),
    getTagGovernanceMetrics: vi.fn(async () => ({
        topTags: [{ tagName: "礼制", contentRefCount: 4 }],
        categoryDistributions: [{ categoryName: "礼学", tagCount: 2 }],
        sourceRatios: [{ source: "MANUAL", tagCount: 1 }],
        monthlyNewTags: [{ month: "2025-01", tagCount: 2 }]
    })),
    changeTagStatus: vi.fn(async () => true),
    createTag: vi.fn(async () => true),
    updateTag: vi.fn(async () => true),
    reviewTag: vi.fn(async () => true),
    reviewBatchTags: vi.fn(async () => true),
    requestTagExtraction: vi.fn(async () => ({
        aiCallId: "501",
        aiCandidateId: "601",
        status: "SUCCEEDED",
        resultFormat: "STRUCTURED",
        resultPayload: '{"tags":[]}',
        candidates: [
            {
                name: "岁时礼俗",
                categoryId: "11",
                categoryName: "礼学",
                confidence: 0.91,
                matchedExistingTagId: null,
                reason: "内容片段集中描述岁时礼俗"
            }
        ]
    })),
    listTagExtractionPromptVersions: vi.fn(async () => [
        {
            id: "301",
            templateId: "201",
            templateName: "知识标签提取",
            capability: "knowledge_tags",
            versionNo: 3
        }
    ]),
    applyExtractedTags: vi.fn(async () => true),
    createTagAlias: vi.fn(async () => true),
    removeTagAlias: vi.fn(async () => true),
    changeCategoryStatus: vi.fn(async () => true),
    createCategory: vi.fn(async () => true),
    updateCategory: vi.fn(async () => true),
    createSynonym: vi.fn(async () => true),
    updateSynonym: vi.fn(async () => true),
    changeSynonymStatus: vi.fn(async () => true),
    removeSynonym: vi.fn(async () => true)
}));

describe("TaxonomyPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["knowledge:taxonomy:view", "knowledge:taxonomy:edit"]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
        queryClient.clear();
        cleanup();
    });

    it("renders tags governance tab", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <TaxonomyPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await userEvent.click(screen.getByRole("tab", { name: "统一标签" }));
        await waitFor(() =>
            expect(service.getTagGovernanceMetrics).toHaveBeenCalledWith({
                topLimit: 10,
                recentMonths: 6
            })
        );
        expect(await screen.findByText("标签治理统计")).toBeInTheDocument();
    }, 30000);

    it("extracts and applies AI tag candidates", async () => {
        const user = userEvent.setup();
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <TaxonomyPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(screen.getByRole("tab", { name: "统一标签" }));
        await user.click(await screen.findByRole("button", { name: "AI 抽取标签" }));

        expect(await screen.findByLabelText("内容类型")).toBeInTheDocument();
        await user.type(screen.getByLabelText("内容 ID"), "SANCAI_ENTRY:1001");
        await user.type(screen.getByLabelText("内容片段"), "正月礼俗与乡饮酒礼相关内容");
        await user.type(screen.getByLabelText("模型 ID"), "100");
        await user.type(screen.getByLabelText("模型名称"), "gpt-5.5");
        await user.click(screen.getByRole("button", { name: "开始抽取" }));

        await waitFor(() => expect(service.requestTagExtraction).toHaveBeenCalled());
        expect(vi.mocked(service.requestTagExtraction).mock.calls[0][0]).toEqual({
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: "SANCAI_ENTRY:1001",
            contentTitle: undefined,
            contentText: "正月礼俗与乡饮酒礼相关内容",
            modelId: "100",
            modelName: "gpt-5.5",
            promptVersionId: undefined,
            maxTags: 10,
            allowNewTags: true
        });

        expect(await screen.findByText("岁时礼俗")).toBeInTheDocument();
        const rowCheckbox = screen.getAllByRole("checkbox").at(-1)!;
        await user.click(rowCheckbox);
        await user.type(screen.getByLabelText("审核备注"), "人工确认后进入审核");
        await user.click(screen.getByRole("button", { name: "应用选中标签" }));

        await waitFor(() => expect(service.applyExtractedTags).toHaveBeenCalled());
        expect(vi.mocked(service.applyExtractedTags).mock.calls[0][0]).toEqual({
            aiCandidateId: "601",
            selectedTags: [
                {
                    name: "岁时礼俗",
                    categoryId: "11",
                    categoryName: "礼学",
                    confidence: 0.91,
                    matchedExistingTagId: null,
                    reason: "内容片段集中描述岁时礼俗"
                }
            ],
            reviewNote: "人工确认后进入审核"
        });
        expect(confirmDangerMock).toHaveBeenCalledWith(
            expect.objectContaining({
                title: "应用 AI 标签候选",
                okText: "应用"
            })
        );
        await waitFor(() =>
            expect(screen.getByRole("tab", { name: "待审核标签" })).toHaveAttribute(
                "aria-selected",
                "true"
            )
        );
    }, 30000);

    it("requires a positive decimal model id before extracting tags", async () => {
        const user = userEvent.setup();
        vi.mocked(service.requestTagExtraction).mockClear();
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <TaxonomyPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(screen.getByRole("tab", { name: "统一标签" }));
        await user.click(await screen.findByRole("button", { name: "AI 抽取标签" }));

        await user.type(screen.getByLabelText("内容 ID"), "SANCAI_ENTRY:1001");
        await user.type(screen.getByLabelText("内容片段"), "正月礼俗与乡饮酒礼相关内容");
        await user.type(screen.getByLabelText("模型 ID"), "abc");
        await user.type(screen.getByLabelText("模型名称"), "gpt-5.5");
        await user.click(screen.getByRole("button", { name: "开始抽取" }));

        expect(await screen.findByText("请输入正整数 ID")).toBeInTheDocument();
        expect(service.requestTagExtraction).not.toHaveBeenCalled();
    }, 30000);

    it("sends prompt version override as string when advanced config is enabled", async () => {
        const user = userEvent.setup();
        replacePermissions([
            "knowledge:taxonomy:view",
            "knowledge:taxonomy:edit",
            "ai:prompt:view"
        ]);
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <TaxonomyPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(screen.getByRole("tab", { name: "统一标签" }));
        await user.click(await screen.findByRole("button", { name: "AI 抽取标签" }));

        await user.type(screen.getByLabelText("内容 ID"), "1001");
        await user.type(screen.getByLabelText("内容片段"), "正月礼俗与乡饮酒礼相关内容");
        await user.type(screen.getByLabelText("模型 ID"), "100");
        await user.type(screen.getByLabelText("模型名称"), "gpt-5.5");
        await user.click(screen.getByRole("button", { name: "覆盖提示词配置" }));
        await user.click(screen.getByLabelText("提示词版本 ID"));
        await user.click(await screen.findByText("知识标签提取 / v3"));
        await user.click(screen.getByRole("button", { name: "开始抽取" }));

        await waitFor(() => expect(service.requestTagExtraction).toHaveBeenCalled());
        expect(vi.mocked(service.requestTagExtraction).mock.calls.at(-1)?.[0]).toEqual(
            expect.objectContaining({
                promptVersionId: "301"
            })
        );
    }, 30000);
});
