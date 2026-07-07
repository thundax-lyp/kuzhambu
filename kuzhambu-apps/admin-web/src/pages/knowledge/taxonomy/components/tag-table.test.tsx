import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { TagTable } from "./tag-table";

const tags = [
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
    }
];

describe("TagTable", () => {
    afterEach(() => {
        cleanup();
    });

    const renderTable = (selectedRowKeys: string[] = []) => {
        const props = {
            canEditTag: true,
            loading: false,
            query: { pageNo: 1, pageSize: 20 },
            selectedRowKeys,
            tags,
            totalCount: tags.length,
            onAdd: vi.fn(),
            onBatchDeprecate: vi.fn(),
            onBatchMerge: vi.fn(),
            onChange: vi.fn(),
            onEdit: vi.fn(),
            onOpenDetail: vi.fn(),
            onRefresh: vi.fn(),
            onSelectedRowKeysChange: vi.fn(),
            onStatusChange: vi.fn()
        };

        render(<TagTable {...props} />);
        return props;
    };

    it("enables batch actions according to selected tag count", async () => {
        const props = renderTable(["1001", "1002"]);

        await userEvent.click(screen.getByRole("button", { name: /批量合并/ }));
        await userEvent.click(screen.getByRole("button", { name: /批量废弃/ }));

        expect(props.onBatchMerge).toHaveBeenCalledTimes(1);
        expect(props.onBatchDeprecate).toHaveBeenCalledTimes(1);
    });

    it("updates selected row keys from table selection", async () => {
        const props = renderTable();
        const rowCheckboxes = screen.getAllByRole("checkbox").slice(1);

        expect(screen.getByRole("button", { name: /批量合并/ })).toBeDisabled();
        expect(screen.getByRole("button", { name: /批量废弃/ })).toBeDisabled();
        await userEvent.click(rowCheckboxes[0]);

        expect(props.onSelectedRowKeysChange).toHaveBeenCalledWith(["1001"]);
    });
});
