import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import * as service from "./taxonomy-service";
import { TaxonomyPage } from "./taxonomy-page";

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
        totalCount: 2,
        totalPage: 1,
        count: 2,
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
    getTagDetail: vi.fn(async () => ({
        tag: null,
        aliases: [],
        contentRefs: []
    })),
    pageSynonyms: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    previewTagMergeImpact: vi.fn(async ({ sourceTagId, targetTagId }) => ({
        sourceTag: {
            id: sourceTagId,
            name: "礼制"
        },
        targetTag: {
            id: targetTagId,
            name: "祭祀"
        },
        aliasesToMerge: [{ id: "2001", name: "礼典", source: "MANUAL" }],
        impactedContentRefs: [
            {
                id: "3001",
                contentTitle: "周礼",
                contentType: "CLASSICS",
                source: "MANUAL"
            }
        ],
        pendingReviewCount: 1,
        governedRecordCount: 3
    })),
    applyTagMerge: vi.fn(async () => true),
    changeTagStatus: vi.fn(async () => true),
    createTag: vi.fn(async () => true),
    updateTag: vi.fn(async () => true),
    reviewTag: vi.fn(async () => true),
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

const openSelectAndChoose = async (label: string, optionText: string) => {
    const select = await screen.findByRole("combobox", { name: label });
    fireEvent.mouseDown(select);
    const options = await screen.findAllByText(optionText);
    await userEvent.click(options.at(-1)!);
};

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

    it("previews and applies tag merge actions", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <TaxonomyPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await userEvent.click(screen.getByRole("tab", { name: "统一标签" }));
        expect(
            await screen.findByRole("heading", { level: 4, name: "标签合并治理" })
        ).toBeInTheDocument();

        await openSelectAndChoose("源标签", "礼制（1001）");
        await openSelectAndChoose("目标标签", "祭祀（1002）");
        await userEvent.click(screen.getByRole("button", { name: "预览合并影响" }));

        await waitFor(() =>
            expect(service.previewTagMergeImpact).toHaveBeenCalledWith(
                {
                    sourceTagId: "1001",
                    targetTagId: "1002"
                },
                expect.anything()
            )
        );
        expect(await screen.findByText("礼典")).toBeInTheDocument();
        expect(screen.getByText("周礼 · CLASSICS")).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "执行标签合并" }));
        await waitFor(() =>
            expect(service.applyTagMerge).toHaveBeenCalledWith(
                {
                    sourceTagId: "1001",
                    targetTagId: "1002"
                },
                expect.anything()
            )
        );
    });
});
