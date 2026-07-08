import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ClassicsContentQaPanel } from "./classics-content-qa-panel";

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: vi.fn(({ onConfirm }: { onConfirm?: () => void }) => onConfirm?.())
    })
}));

vi.mock("@/components/kuzhambu-table", () => {
    const kuzhambuTableMock = ({
        ariaLabel,
        columns,
        dataSource,
        onSort
    }: {
        ariaLabel?: string;
        columns?: Array<{
            key?: string;
            render?: (value: unknown, record: unknown) => JSX.Element;
        }>;
        dataSource?: unknown[];
        onSort?: (source: unknown, target: unknown, position: "before" | "after") => void;
    }) => {
        const actionColumn = columns?.find((column) => column.key === "actions");
        const rows = Array.isArray(dataSource) ? dataSource : [];

        const triggerSort = () => {
            if (!onSort || rows.length < 2) {
                return;
            }
            onSort(rows[0], rows[1], "before");
        };

        return (
            <div aria-label={ariaLabel}>
                <button aria-label="模拟排序" onClick={triggerSort}>
                    排序
                </button>
                {rows.map((row, index) => (
                    <div key={index}>{actionColumn?.render?.(undefined, row)}</div>
                ))}
            </div>
        );
    };

    return {
        __esModule: true,
        KuzhambuTable: kuzhambuTableMock
    };
});

const apiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data }), {
            headers: { "Content-Type": "application/json" },
            status: 200
        })
    );

interface CapturedCall {
    body: unknown;
    method: string | undefined;
    path: string;
}

const capturedCalls: CapturedCall[] = [];

const readFetchUrl = (input: RequestInfo | URL) => {
    if (typeof input === "string") {
        return input;
    }
    if (input instanceof URL) {
        return input.href;
    }
    return input.url;
};

const readFetchBody = (body: BodyInit | null | undefined) => {
    if (!body) {
        return undefined;
    }
    return JSON.parse(String(body));
};

const installFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
        const path = readFetchUrl(input).replace("/kuzhambu-admin-api/api", "");
        const method = init?.method ?? "GET";
        const body = readFetchBody(init?.body);

        capturedCalls.push({
            body,
            method,
            path
        });

        if (
            path.includes("/classics/content/qa-pairs") &&
            path.includes("contentType=WANGQI_DOCUMENT")
        ) {
            return apiResponse([
                {
                    id: 10001,
                    question: "已有问题",
                    answer: "已有答案",
                    source: "MANUAL"
                },
                {
                    id: 10002,
                    question: "已有问题2",
                    answer: "已有答案2",
                    source: "MANUAL"
                }
            ]);
        }

        if (path.endsWith("/classics/content/qa-pairs/add")) {
            return apiResponse({
                id: 10002,
                ...((body as Record<string, unknown>) ?? {})
            });
        }

        if (path.endsWith("/classics/content/qa-pairs/update")) {
            return apiResponse({
                ...((body as Record<string, unknown>) ?? {}),
                id: 10001
            });
        }

        if (path.endsWith("/classics/content/qa-pairs/delete")) {
            return apiResponse(true);
        }

        if (path.endsWith("/classics/content/qa-pairs/sort")) {
            return apiResponse(true);
        }

        return apiResponse(true);
    });
};

describe("ClassicsContentQaPanel", () => {
    const createTestQueryClient = () =>
        new QueryClient({
            defaultOptions: {
                queries: {
                    retry: false
                }
            }
        });

    beforeEach(() => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.accessTokenExpireAt",
            String(Date.now() + 3600 * 1000)
        );
        capturedCalls.length = 0;
        installFetchMock();
    });

    afterEach(() => {
        cleanup();
        vi.restoreAllMocks();
        localStorage.clear();
    });

    it("adds qa pair with required fields and sends add api", async () => {
        const user = userEvent.setup();
        const queryClient = createTestQueryClient();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <ClassicsContentQaPanel
                        contentId={4001}
                        contentType="WANGQI_DOCUMENT"
                        onChanged={vi.fn()}
                    />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByRole("button", { name: "新增问答对" }));
        const dialog = screen.getByRole("dialog", { name: "新增问答对" });
        expect(await within(dialog).findByRole("button", { name: "OK" })).toBeInTheDocument();

        await user.type(within(dialog).getByLabelText("问答问题"), "新问题");
        await user.type(within(dialog).getByLabelText("问答答案"), "新答案");
        await user.click(within(dialog).getByRole("button", { name: "OK" }));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    method: "POST",
                    path: "/classics/content/qa-pairs/add",
                    body: expect.objectContaining({
                        contentType: "WANGQI_DOCUMENT",
                        contentId: 4001,
                        question: "新问题",
                        answer: "新答案",
                        source: "MANUAL"
                    })
                })
            );
        });
    }, 30000);

    it("edits qa pair and sends update api", async () => {
        const user = userEvent.setup();
        const queryClient = createTestQueryClient();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <ClassicsContentQaPanel
                        contentId={4001}
                        contentType="WANGQI_DOCUMENT"
                        onChanged={vi.fn()}
                    />
                </AntdApp>
            </QueryClientProvider>
        );

        const editButton = await screen.findByRole("button", { name: "编辑问答对 10001" });
        await user.click(editButton);

        const editDialog = screen.getByRole("dialog", { name: "编辑问答对" });
        await user.clear(within(editDialog).getByLabelText("问答答案"));
        await user.type(within(editDialog).getByLabelText("问答答案"), "已修订答案");
        await user.click(within(editDialog).getByRole("button", { name: "OK" }));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    method: "POST",
                    path: "/classics/content/qa-pairs/update",
                    body: expect.objectContaining({
                        id: 10001,
                        answer: "已修订答案"
                    })
                })
            );
        });
    }, 30000);

    it("deletes qa pair with confirmation and sends delete api", async () => {
        const user = userEvent.setup();
        const queryClient = createTestQueryClient();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <ClassicsContentQaPanel
                        contentId={4001}
                        contentType="WANGQI_DOCUMENT"
                        onChanged={vi.fn()}
                    />
                </AntdApp>
            </QueryClientProvider>
        );

        const deleteButton = await screen.findByRole("button", { name: "删除问答对 10001" });
        await user.click(deleteButton);

        await waitFor(() => {
            expect(capturedCalls).toContainEqual({
                body: { id: 10001 },
                method: "POST",
                path: "/classics/content/qa-pairs/delete"
            });
        });
    }, 30000);

    it("sorts qa pairs and sends sort api without version confirmation", async () => {
        const user = userEvent.setup();
        const queryClient = createTestQueryClient();
        const onChanged = vi.fn();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <ClassicsContentQaPanel
                        contentId={4001}
                        contentType="WANGQI_DOCUMENT"
                        onChanged={onChanged}
                    />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByRole("button", { name: "模拟排序" }));

        await waitFor(() => {
            expect(capturedCalls).toContainEqual(
                expect.objectContaining({
                    method: "POST",
                    path: "/classics/content/qa-pairs/sort",
                    body: {
                        orderedIds: [10002, 10001],
                        sortDirection: "ASC"
                    }
                })
            );
            expect(onChanged).toHaveBeenCalled();
        });
    }, 30000);
});
