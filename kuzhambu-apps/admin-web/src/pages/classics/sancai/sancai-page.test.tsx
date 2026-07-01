import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { SancaiPage } from "./sancai-page";

const apiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data }), {
            headers: { "Content-Type": "application/json" },
            status: 200
        })
    );

const readFetchUrl = (input: RequestInfo | URL) => {
    if (typeof input === "string") {
        return input;
    }
    if (input instanceof URL) {
        return input.href;
    }
    return input.url;
};

const installFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
        const path = readFetchUrl(input).replace("/kuzhambu-admin-api/api", "");

        if (path.endsWith("/classics/sancai/categories/list")) {
            return apiResponse([{ categoryType: "FORMAL", id: 2, title: "天文" }]);
        }
        if (path.endsWith("/classics/sancai/categories/types")) {
            return apiResponse([
                { label: "正式门类", type: "SANCAI_CATEGORY_TYPE", value: "FORMAL" }
            ]);
        }
        if (path.endsWith("/classics/sancai/volumes/types")) {
            return apiResponse([{ label: "正式卷目", type: "SANCAI_VOLUME_TYPE", value: "MAIN" }]);
        }
        if (path.endsWith("/classics/sancai/volumes/list")) {
            return apiResponse([{ categoryId: 2, id: 101, title: "天文卷一", volumeType: "MAIN" }]);
        }
        if (path.endsWith("/classics/sancai/entries/page")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 20,
                totalCount: 0,
                totalPage: 0,
                records: []
            });
        }
        if (path.endsWith("/classics/sancai/entries/list")) {
            return apiResponse([
                {
                    id: 3001,
                    volumeId: 101,
                    title: "天地",
                    originalText: "天地玄黄",
                    translationText: "译文",
                    summary: "天地摘要",
                    lifecycleStatus: "PUBLISHED",
                    visibility: "PUBLIC",
                    translationStatus: "READY",
                    imageStatus: "READY",
                    visualAssetStatus: "READY",
                    refinementStatus: "COMPLETE",
                    currentVersionId: 9001,
                    currentVersionNo: 1,
                    currentVersionedAt: "2026-06-20T01:00:00.000+08:00",
                    contentUpdatedAt: "2026-06-20T01:00:00.000+08:00",
                    versionDirty: false
                }
            ]);
        }
        if (path.endsWith("/classics/sancai/entries/3001")) {
            return apiResponse({
                id: 3001,
                volumeId: 101,
                title: "天地",
                originalText: "天地玄黄",
                translationText: "译文",
                summary: "天地摘要",
                lifecycleStatus: "PUBLISHED",
                visibility: "PUBLIC",
                translationStatus: "READY",
                imageStatus: "READY",
                visualAssetStatus: "READY",
                refinementStatus: "COMPLETE",
                currentVersionId: 9001,
                currentVersionNo: 1,
                currentVersionedAt: "2026-06-20T01:00:00.000+08:00",
                contentUpdatedAt: "2026-06-20T01:00:00.000+08:00",
                versionDirty: false
            });
        }
        if (path.endsWith("/classics/sancai/assets/images/3001")) {
            return apiResponse([
                {
                    currentUsed: true,
                    entryId: 3001,
                    id: 8001,
                    imageType: "ORIGINAL",
                    originalFilename: "sancai.png",
                    priority: 1,
                    size: 10,
                    storageObjectId: 7001,
                    title: "sancai.png"
                }
            ]);
        }
        if (path.endsWith("/classics/sancai/assets/visual-assets/3001")) {
            return apiResponse([
                {
                    id: 5002,
                    visualAssetId: 5002,
                    entryId: 3001,
                    versionNo: 2,
                    status: "READY",
                    sourcePreviewUrl: "/api/storage/object/7001/content",
                    sourceDownloadUrl: "/api/storage/object/7001/content?download=true",
                    generatedPreviewUrl: "/api/storage/object/7002/content",
                    generatedDownloadUrl: "/api/storage/object/7002/content?download=true",
                    currentUsed: true,
                    textWeight: 60,
                    imageWeight: 40,
                    imageAnalysisMarkdown: "图片理解",
                    fusionDescription: "融合说明",
                    visualDescription: "视觉描述",
                    generationParamsJson: '{"style":"gongbi"}'
                }
            ]);
        }
        if (path.endsWith("/classics/sancai/entries/versions/list")) {
            return apiResponse([
                {
                    id: 9001,
                    contentType: "SANCAI_ENTRY",
                    contentId: 3001,
                    versionNo: 1,
                    versionedAt: "2026-06-20T01:00:00.000+08:00",
                    snapshotJson: '{"title":"天地"}',
                    changeType: "MANUAL_SAVE",
                    changeSummary: "手动保存"
                }
            ]);
        }
        if (path.endsWith("/sys/current-user/info")) {
            return apiResponse({
                id: 99,
                loginName: "admin",
                name: "Admin"
            });
        }
        if (path.endsWith("/ai/refinement/task/page")) {
            return apiResponse({
                items: [],
                total: 0,
                pageNo: 1,
                pageSize: 20
            });
        }
        if (path.includes("/classics/content/tags?")) {
            return apiResponse([]);
        }
        if (path.includes("/classics/content/qa-pairs?")) {
            return apiResponse([]);
        }
        if (path.endsWith("/ai/invocation/candidate/list")) {
            return apiResponse([]);
        }

        return apiResponse(true);
    });
};

describe("SancaiPage", () => {
    beforeEach(() => {
        queryClient.clear();
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        installFetchMock();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it("renders page and category tree content", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SancaiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "三才图会" })).toBeInTheDocument();
        expect(await screen.findByRole("link", { name: "打开门类 天文" })).toBeInTheDocument();
    }, 10000);

    it("opens entry detail from the page and keeps the visual asset section available", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SancaiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByRole("link", { name: "打开门类 天文" }));
        const volumeTable = await screen.findByLabelText("三才图会卷目表格");
        await user.click(
            await within(volumeTable).findByRole("link", { name: "打开卷目 天文卷一" })
        );

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        expect(await screen.findByLabelText("三才图会视觉资产面板")).toBeInTheDocument();
        expect(await screen.findByRole("button", { name: "保存视觉资产字段" })).toBeInTheDocument();
        expect(await screen.findByRole("button", { name: "设为当前使用版本" })).toBeDisabled();
    }, 15000);
});
