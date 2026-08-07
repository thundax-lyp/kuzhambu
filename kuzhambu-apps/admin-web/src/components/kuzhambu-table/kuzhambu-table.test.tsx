import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KuzhambuTable } from "./kuzhambu-table";

interface RowItem {
    id: string;
    name: string;
    status: string;
}

describe("KuzhambuTable", () => {
    afterEach(() => {
        cleanup();
        vi.mocked(window.matchMedia).mockReset();
        vi.mocked(window.matchMedia).mockImplementation((query: string) => ({
            matches: false,
            media: query,
            onchange: null,
            addListener: vi.fn(),
            removeListener: vi.fn(),
            addEventListener: vi.fn(),
            removeEventListener: vi.fn(),
            dispatchEvent: vi.fn()
        }));
    });

    it("keeps one flexible content column when all content columns specify widths", () => {
        const { container } = render(
            <KuzhambuTable<RowItem>
                ariaLabel="规则表格"
                columns={[
                    {
                        dataIndex: "name",
                        key: "name",
                        title: "名称",
                        width: 240
                    },
                    {
                        dataIndex: "status",
                        key: "status",
                        title: "状态",
                        width: 120
                    },
                    {
                        key: "actions",
                        title: "操作",
                        width: 96,
                        options: [
                            {
                                key: "edit",
                                text: "编辑",
                                testId: "kuzhambu-table-test-edit-button",
                                onClick: () => undefined
                            }
                        ]
                    }
                ]}
                dataSource={[{ id: "1", name: "条目", status: "启用" }]}
                pagination={false}
                rowKey="id"
            />
        );

        const columns = Array.from(container.querySelectorAll("col"));

        expect(columns[0]).not.toHaveStyle({ width: "240px" });
        expect(columns[1]).toHaveStyle({ width: "120px" });
        expect(columns[2]).toHaveStyle({ width: "96px" });
    });

    it("keeps a single row action inline on compact screens", () => {
        vi.mocked(window.matchMedia).mockImplementation((query: string) => ({
            matches: true,
            media: query,
            onchange: null,
            addListener: vi.fn(),
            removeListener: vi.fn(),
            addEventListener: vi.fn(),
            removeEventListener: vi.fn(),
            dispatchEvent: vi.fn()
        }));

        render(
            <KuzhambuTable<RowItem>
                ariaLabel="规则表格"
                columns={[
                    {
                        dataIndex: "name",
                        key: "name",
                        title: "名称"
                    },
                    {
                        key: "actions",
                        title: "操作",
                        options: [
                            {
                                key: "delete-divider",
                                type: "divider"
                            },
                            {
                                key: "delete",
                                text: "删除",
                                testId: "kuzhambu-table-test-delete-button",
                                onClick: () => undefined
                            }
                        ]
                    }
                ]}
                dataSource={[{ id: "1", name: "条目", status: "启用" }]}
                pagination={false}
                rowKey="id"
            />
        );

        expect(screen.getByTestId("kuzhambu-table-test-delete-button")).toHaveTextContent("删除");
        expect(screen.queryByRole("button", { name: "展开行操作" })).not.toBeInTheDocument();
    });
});
