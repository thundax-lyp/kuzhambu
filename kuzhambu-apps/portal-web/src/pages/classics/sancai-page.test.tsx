import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { SancaiPage } from "./sancai-page";

const apiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data }), {
            headers: { "Content-Type": "application/json" },
            status: 200
        })
    );

const installFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
        const url = String(input);

        if (url.includes("/portal/classics/sancai/categories")) {
            return apiResponse([{ categoryType: "FORMAL", id: 1, title: "天地" }]);
        }
        if (url.includes("/portal/classics/sancai/volumes")) {
            return apiResponse([{ categoryId: 1, id: 11, title: "卷一", volumeType: "MAIN" }]);
        }
        if (url.includes("/portal/classics/sancai/entries/1001")) {
            return apiResponse({
                id: 1001,
                volumeId: 11,
                title: "天",
                originalText: "天者，万物之始。",
                translationText: "天是万物的开端。",
                summary: "天地门条目",
                lifecycleStatus: "PUBLISHED",
                visibility: "PUBLIC"
            });
        }
        if (url.includes("/portal/classics/sancai/entries")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 12,
                totalCount: 1,
                totalPage: 1,
                records: [
                    {
                        id: 1001,
                        volumeId: 11,
                        title: "天",
                        originalText: "天者，万物之始。",
                        translationText: "天是万物的开端。",
                        summary: "天地门条目",
                        lifecycleStatus: "PUBLISHED",
                        visibility: "PUBLIC"
                    }
                ]
            });
        }

        return apiResponse(null);
    });
};

const renderPage = () => {
    const client = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

    render(
        <QueryClientProvider client={client}>
            <SancaiPage />
        </QueryClientProvider>
    );
};

describe("SancaiPage", () => {
    beforeEach(() => {
        installFetchMock();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("renders public sancai catalog and entry detail", async () => {
        const user = userEvent.setup();
        renderPage();

        expect(await screen.findByRole("heading", { name: "三才图会" })).toBeTruthy();
        await user.click(await screen.findByRole("button", { name: "天地" }));
        await user.click(await screen.findByRole("button", { name: "卷一" }));

        const list = await screen.findByLabelText("三才图会公开条目列表");
        await user.click(await within(list).findByRole("button", { name: /天/ }));

        const detail = await screen.findByLabelText("三才图会条目详情");
        await waitFor(() => {
            expect(within(detail).getByText("天是万物的开端。")).toBeTruthy();
        });
    });
});
