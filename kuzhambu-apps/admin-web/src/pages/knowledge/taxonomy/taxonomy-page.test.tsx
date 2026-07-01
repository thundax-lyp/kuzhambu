import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
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
        totalCount: 1,
        totalPage: 1,
        count: 1,
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
    deprecateTag: vi.fn(async () => true),
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
});
