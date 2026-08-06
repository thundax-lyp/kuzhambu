import { render, screen } from "@testing-library/react";
import type { ComponentProps } from "react";
import { describe, expect, it, vi } from "vitest";
import { WangqiDocumentTable } from "./wangqi-document-table";

const renderTable = (dataSource: ComponentProps<typeof WangqiDocumentTable>["dataSource"]) => {
    render(
        <WangqiDocumentTable
            canChangeDocumentPublication
            dataSource={dataSource}
            isPublicationChanging={false}
            onDelete={vi.fn()}
            onExport={vi.fn()}
            onOpenEdit={vi.fn()}
            onOpenBatchCandidateDrawer={vi.fn()}
            onPublicationAction={vi.fn()}
            onPublicationBatch={vi.fn()}
            onSelectedDocumentIdsChange={vi.fn()}
            onSortDirectionChange={vi.fn()}
            pagination={false}
            selectedDocumentIds={[]}
            sortDirection="DESC"
        />
    );
};

describe("WangqiDocumentTable", () => {
    it("renders publication and transition statuses in Chinese", () => {
        renderTable([
            { id: "draft", title: "草稿文档", lifecycleStatus: "DRAFT" },
            { id: "published", title: "发布文档", lifecycleStatus: "PUBLISHED" },
            { id: "offline", title: "下线文档", lifecycleStatus: "OFFLINE" },
            { id: "error", title: "异常文档", lifecycleStatus: "ERROR" },
            {
                id: "publishing",
                title: "发布中文档",
                lifecycleStatus: "DRAFT",
                transitionStatus: "PUBLISHING"
            },
            {
                id: "offlining",
                title: "下线中文档",
                lifecycleStatus: "PUBLISHED",
                transitionStatus: "OFFLINING"
            }
        ]);

        expect(screen.getAllByText("草稿")).toHaveLength(2);
        expect(screen.getAllByText("已发布")).toHaveLength(2);
        expect(screen.getByText("已下线")).toBeInTheDocument();
        expect(screen.getByText("发布异常")).toBeInTheDocument();
        expect(screen.getByText("发布中")).toBeInTheDocument();
        expect(screen.getByText("下线中")).toBeInTheDocument();
    });

    it("keeps unknown status codes visible for forward compatibility", () => {
        renderTable([
            {
                id: "unknown",
                title: "未知状态文档",
                lifecycleStatus: "ARCHIVED",
                transitionStatus: "ARCHIVING"
            }
        ]);

        expect(screen.getByText("ARCHIVED")).toBeInTheDocument();
        expect(screen.getByText("ARCHIVING")).toBeInTheDocument();
    });
});
