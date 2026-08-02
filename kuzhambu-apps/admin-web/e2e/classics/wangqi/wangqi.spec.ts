import { expect, test } from "@playwright/test";
import type { Page, Route } from "@playwright/test";

const ADMIN_PERMISSIONS = [
    "classics:wangqi:view",
    "classics:wangqi:edit",
    "classics:wangqi:delete"
];

test.use({ viewport: { width: 1280, height: 800 }, isMobile: false });

const readRequestBody = (postData: string | null) => {
    return postData ? (JSON.parse(postData) as Record<string, unknown>) : {};
};

const apiResponse = (data: unknown) => ({
    code: "COMMON-00000",
    message: "success",
    data
});

const fulfillSuccess = async (route: Route, data: unknown) => {
    await route.fulfill({
        contentType: "application/json",
        body: JSON.stringify(apiResponse(data))
    });
};

const readTaskIdForCapability = (capability: unknown) => {
    if (capability === "qa") {
        return 9003;
    }
    if (capability === "tags") {
        return 9002;
    }
    return 9001;
};

const readCapabilityForTaskId = (taskId: unknown) => {
    if (taskId === 9003) {
        return "qa";
    }
    if (taskId === 9002) {
        return "tags";
    }
    return "summary";
};

const readCandidateIdForCapability = (capability: string) => {
    if (capability === "qa") {
        return 7001;
    }
    if (capability === "tags") {
        return 6001;
    }
    return 5001;
};

const mockShellApis = async (page: Page) => {
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/info", async (route) => {
        await fulfillSuccess(route, { id: "1", loginName: "developer", name: "Developer" });
    });
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/menus", async (route) => {
        await fulfillSuccess(route, [
            { id: "20", name: "古籍管理", displayParams: '{"icon":"classics"}' },
            {
                id: "23",
                parentId: "20",
                name: "王圻文档",
                url: "/classics/wangqi",
                displayParams: '{"icon":"wangqi"}'
            }
        ]);
    });
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/perms", async (route) => {
        await fulfillSuccess(route, {
            perms: ADMIN_PERMISSIONS
        });
    });
    await page.route("**/kuzhambu-admin-api/api/auth/session/token/refresh", async (route) => {
        await fulfillSuccess(route, {
            token: "test-token",
            refreshToken: "refresh-token",
            expireAt: Date.now() + 3600 * 1000
        });
    });
};

test.describe("classics wangqi page", () => {
    test.skip(({ isMobile }) => isMobile, "Wangqi admin table workflow is desktop-only.");

    test.beforeEach(async ({ page }) => {
        await mockShellApis(page);
        await page.addInitScript(() => {
            window.localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
            window.localStorage.setItem("kuzhambu.admin.refreshToken", "refresh-token");
            window.localStorage.setItem(
                "kuzhambu.admin.accessTokenExpireAt",
                String(Date.now() + 3600 * 1000)
            );
            window.localStorage.setItem(
                "kuzhambu.admin.permissions",
                JSON.stringify([
                    "classics:wangqi:view",
                    "classics:wangqi:edit",
                    "classics:wangqi:delete"
                ])
            );
        });
    });

    test("links Wangqi list, editor, file and versions to APIs", async ({ page }) => {
        test.setTimeout(60_000);

        const pageRequests: Array<Record<string, unknown>> = [];
        const refinementTaskRequests: Array<Record<string, unknown>> = [];
        const summaryCandidateRequests: Array<Record<string, unknown>> = [];
        const tagCandidateRequests: Array<Record<string, unknown>> = [];
        const qaCandidateRequests: Array<Record<string, unknown>> = [];
        const candidateApplyRequests: Array<Record<string, unknown>> = [];
        const updateRequests: Array<Record<string, unknown>> = [];
        const uploadRequests: Array<string> = [];
        const resetRequests: Array<Record<string, unknown>> = [];
        const deleteRequests: Array<Record<string, unknown>> = [];
        let refinementTaskPageCount = 0;

        const record = {
            id: 1,
            title: "王圻文档",
            summary: "记录王圻古籍条目。",
            contentFormat: "MARKDOWN",
            content: "## 王圻\n\n古籍正文。",
            documentTime: "2026-01-01T00:00:00.000+00:00",
            storageObjectId: 7001,
            visibility: "PUBLIC"
        };

        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/page",
            async (route) => {
                const body = readRequestBody(route.request().postData());
                pageRequests.push(body);
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            pageNo: 1,
                            pageSize: 20,
                            totalCount: 1,
                            count: 1,
                            totalPage: 1,
                            records: [{ ...record, visibility: body.visibility ?? "PUBLIC" }]
                        })
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/timeline/list",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(apiResponse([record]))
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/content/exports/page",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            pageNo: 1,
                            pageSize: 20,
                            totalCount: 0,
                            count: 0,
                            totalPage: 0,
                            records: []
                        })
                    )
                });
            }
        );
        await page.route("**/kuzhambu-admin-api/api/classics/content/tags/list", async (route) => {
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse([
                        {
                            id: 8101,
                            tagId: 9101,
                            contentType: "WANGQI_DOCUMENT",
                            contentId: 1,
                            tagNameSnapshot: "史部",
                            source: "MANUAL",
                            status: "ACTIVE"
                        }
                    ])
                )
            });
        });
        await page.route(
            "**/kuzhambu-admin-api/api/classics/content/qa-pairs/list",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse([
                            {
                                id: 8201,
                                contentType: "WANGQI_DOCUMENT",
                                contentId: 1,
                                question: "已有问题？",
                                answer: "已有答案。",
                                source: "MANUAL"
                            }
                        ])
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/get",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            ...record,
                            contentFormat: "HTML",
                            content: "<h2>王圻</h2><script>alert(1)</script>"
                        })
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/source-file/get",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            documentId: 1,
                            storageObjectId: 7001,
                            originalFilename: "wangqi.pdf",
                            contentType: "application/pdf",
                            size: 10,
                            contentUrl: "/classics/wangqi/documents/1/source-file/content"
                        })
                    )
                });
            }
        );
        await page.route("**/kuzhambu-admin-api/api/ai/refinement/task/page", async (route) => {
            refinementTaskPageCount += 1;
            refinementTaskRequests.push(readRequestBody(route.request().postData()));
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({
                        items: [
                            {
                                taskId: 9001,
                                status: refinementTaskPageCount === 1 ? "RUNNING" : "SUCCEEDED",
                                capability: "summary",
                                contentType: "WANGQI_DOCUMENT",
                                contentId: 1,
                                requestedAt: "2026-01-01T00:00:00.000+00:00"
                            }
                        ],
                        total: 1,
                        pageNo: 1,
                        pageSize: 10
                    })
                )
            });
        });
        await page.route("**/kuzhambu-admin-api/api/ai/refinement/task/add", async (route) => {
            const body = readRequestBody(route.request().postData());
            const capability = String(body.capability || "summary");
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({
                        taskId: readTaskIdForCapability(capability),
                        status: "PENDING",
                        capability,
                        contentType: "WANGQI_DOCUMENT",
                        contentId: 1
                    })
                )
            });
        });
        await page.route("**/kuzhambu-admin-api/api/ai/refinement/task/get", async (route) => {
            const body = readRequestBody(route.request().postData());
            const capability = readCapabilityForTaskId(body.taskId);
            await route.fulfill({
                contentType: "application/json",
                body: JSON.stringify(
                    apiResponse({
                        taskId: readTaskIdForCapability(capability),
                        status: "SUCCEEDED",
                        capability,
                        contentType: "WANGQI_DOCUMENT",
                        contentId: 1,
                        candidateId: readCandidateIdForCapability(capability),
                        requestedAt: "2026-01-01T00:00:00.000+00:00"
                    })
                )
            });
        });
        await page.route(
            "**/kuzhambu-admin-api/api/ai/invocation/candidate/list",
            async (route) => {
                const body = readRequestBody(route.request().postData());
                if (body.capability === "tags") {
                    tagCandidateRequests.push(body);
                    await route.fulfill({
                        contentType: "application/json",
                        body: JSON.stringify(
                            apiResponse([
                                {
                                    candidateId: 6001,
                                    contentType: "WANGQI_DOCUMENT",
                                    contentId: 1,
                                    capability: "tags",
                                    resultFormat: "STRUCTURED",
                                    resultPayload: JSON.stringify({ tags: ["经部", "文献"] }),
                                    status: "PENDING",
                                    requestedAt: "2026-01-01T00:00:02.000+00:00"
                                }
                            ])
                        )
                    });
                    return;
                }
                if (body.capability === "qa") {
                    qaCandidateRequests.push(body);
                    await route.fulfill({
                        contentType: "application/json",
                        body: JSON.stringify(
                            apiResponse([
                                {
                                    candidateId: 7001,
                                    contentType: "WANGQI_DOCUMENT",
                                    contentId: 1,
                                    capability: "qa",
                                    resultFormat: "STRUCTURED",
                                    resultPayload: JSON.stringify({
                                        qaPairs: [
                                            {
                                                question: "王圻是谁？",
                                                answer: "王圻是明代学者。"
                                            }
                                        ]
                                    }),
                                    status: "PENDING",
                                    requestedAt: "2026-01-01T00:00:03.000+00:00"
                                }
                            ])
                        )
                    });
                    return;
                }
                summaryCandidateRequests.push(body);
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse([
                            {
                                candidateId: 5001,
                                contentType: "WANGQI_DOCUMENT",
                                contentId: 1,
                                capability: "summary",
                                resultFormat: "TEXT",
                                resultPayload: "任务完成后的摘要候选",
                                status: "PENDING",
                                requestedAt: "2026-01-01T00:00:01.000+00:00"
                            }
                        ])
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/content/ai-candidates/change",
            async (route) => {
                candidateApplyRequests.push(readRequestBody(route.request().postData()));
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            contentType: "WANGQI_DOCUMENT",
                            contentId: 1,
                            versionId: 9101,
                            versionNo: 2
                        })
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/1/source-file/upload",
            async (route) => {
                uploadRequests.push(route.request().method());
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(apiResponse({ documentId: 1, storageObjectId: 7002 }))
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/versions/list",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse([
                            {
                                id: 9001,
                                versionNo: 1,
                                versionedAt: "2026-01-01T00:00:00.000+00:00",
                                snapshotJson: JSON.stringify({
                                    title: "历史王圻文档",
                                    storageObjectId: 6001
                                }),
                                changeType: "MANUAL_SAVE",
                                changeSummary: "保存王圻文档"
                            }
                        ])
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/versions/get",
            async (route) => {
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(
                        apiResponse({
                            id: 9001,
                            versionNo: 1,
                            snapshotJson: JSON.stringify({
                                title: "历史王圻文档",
                                storageObjectId: 6001
                            }),
                            changeType: "MANUAL_SAVE"
                        })
                    )
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/versions/reset",
            async (route) => {
                resetRequests.push(readRequestBody(route.request().postData()));
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(apiResponse({ id: 9002, versionNo: 2 }))
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/update",
            async (route) => {
                updateRequests.push(readRequestBody(route.request().postData()));
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(apiResponse({ id: 1 }))
                });
            }
        );
        await page.route(
            "**/kuzhambu-admin-api/api/classics/wangqi/documents/delete",
            async (route) => {
                deleteRequests.push(readRequestBody(route.request().postData()));
                await route.fulfill({
                    contentType: "application/json",
                    body: JSON.stringify(apiResponse(true))
                });
            }
        );

        await page.setViewportSize({ width: 1280, height: 800 });
        await page.goto("/classics/wangqi");

        await expect(page.getByRole("heading", { name: "王圻文档" })).toBeVisible();
        await expect(page.getByRole("table", { name: "王圻文档表格" })).toBeVisible();
        await expect
            .poll(() => pageRequests.at(-1))
            .toEqual({
                pageNo: 1,
                pageSize: 20,
                sortDirection: "DESC"
            });

        await page.getByTestId("wangqi-document-edit-1-button").click();
        await expect(page.getByLabel("王圻 Tiptap 编辑器")).toBeVisible();
        await expect(page.getByLabel("王圻文档正文", { exact: true })).not.toContainText(
            "alert(1)"
        );
        await page.getByRole("button", { name: "AI 摘要" }).click();
        await page.getByTestId("classics-wangqi-document-summary-ai-generate-button").click();
        await expect(page.getByText("摘要任务已完成")).toBeVisible();
        await expect(page.getByText("候选摘要加载失败")).toBeHidden();
        await expect(page.getByLabel("AI摘要候选摘要")).toHaveValue("任务完成后的摘要候选");
        await expect
            .poll(() => refinementTaskRequests.at(-1))
            .toMatchObject({
                contentType: "WANGQI_DOCUMENT",
                contentId: 1
            });
        await expect
            .poll(() => summaryCandidateRequests.at(-1))
            .toMatchObject({
                contentType: "WANGQI_DOCUMENT",
                contentId: 1,
                capability: "summary"
            });
        await page
            .getByTestId("classics-wangqi-document-summary-ai-modal")
            .getByRole("button", { name: /^\s*取\s*消\s*$/ })
            .click();

        await page.getByLabel("编辑王圻文档", { exact: true }).getByText("标签").click();
        await page.getByTestId("classics-wangqi-document-tags-ai-button").click();
        await expect(page.getByRole("dialog", { name: "AI 标签" })).toBeVisible();
        await expect(page.getByLabel("AI标签依据标题")).toHaveValue("王圻文档");
        await expect(page.getByLabel("AI标签依据摘要")).toHaveValue("记录王圻古籍条目。");
        await expect(page.getByLabel("AI标签依据正文")).toHaveValue(
            "<h2>王圻</h2><script>alert(1)</script>"
        );
        await expect(page.getByLabel("AI标签依据已有标签")).toContainText("史部");
        await page.getByTestId("classics-wangqi-document-tags-ai-generate-button").click();
        await expect(page.getByText("标签任务已完成")).toBeVisible();
        await expect(page.getByLabel("候选标签 1")).toHaveValue("经部");
        await expect(page.getByLabel("候选标签 2")).toHaveValue("文献");
        await page.getByTestId("classics-wangqi-document-tags-ai-apply-button").click();
        await expect
            .poll(() => tagCandidateRequests.at(-1))
            .toMatchObject({
                contentType: "WANGQI_DOCUMENT",
                contentId: 1,
                capability: "tags"
            });
        await expect
            .poll(() => candidateApplyRequests.at(-1))
            .toMatchObject({
                candidateId: "6001",
                contentType: "WANGQI_DOCUMENT",
                contentId: 1,
                capability: "tags",
                resultFormat: "STRUCTURED",
                resultPayload: JSON.stringify({ tags: ["经部", "文献"] })
            });

        await page.getByLabel("编辑王圻文档", { exact: true }).getByText("问答").click();
        await page.getByTestId("classics-wangqi-document-qa-ai-button").click();
        await expect(page.getByRole("dialog", { name: "问答生成" })).toBeVisible();
        await expect(page.getByLabel("问答依据标题")).toHaveValue("王圻文档");
        await expect(page.getByLabel("问答依据摘要")).toHaveValue("记录王圻古籍条目。");
        await expect(page.getByLabel("问答依据正文")).toHaveValue(
            "<h2>王圻</h2><script>alert(1)</script>"
        );
        await expect(page.getByLabel("问答依据已有问答")).toContainText("已有问题？");
        await page.getByTestId("classics-wangqi-document-qa-ai-generate-button").click();
        await expect(page.getByText("问答任务已完成")).toBeVisible();
        await expect(page.getByLabel("问答问题 1")).toHaveValue("王圻是谁？");
        await expect(page.getByLabel("问答答案 1")).toHaveValue("王圻是明代学者。");
        await page.getByTestId("classics-wangqi-document-qa-ai-apply-button").click();
        await expect
            .poll(() => qaCandidateRequests.at(-1))
            .toMatchObject({
                contentType: "WANGQI_DOCUMENT",
                contentId: 1,
                capability: "qa"
            });
        await expect
            .poll(() => candidateApplyRequests.at(-1))
            .toMatchObject({
                candidateId: "7001",
                contentType: "WANGQI_DOCUMENT",
                contentId: 1,
                capability: "qa",
                resultFormat: "STRUCTURED",
                resultPayload: JSON.stringify({
                    qaPairs: [{ question: "王圻是谁？", answer: "王圻是明代学者。" }]
                })
            });

        await page.getByLabel("编辑王圻文档", { exact: true }).getByText("基础信息").click();
        await page.getByRole("textbox", { name: "王圻文档标题" }).fill("王圻文档修订");
        await page.getByTestId("classics-wangqi-document-save-button").click();
        await expect
            .poll(() => updateRequests.at(-1))
            .toMatchObject({
                id: 1,
                title: "王圻文档修订"
            });

        await page.getByTestId("wangqi-document-edit-1-button").click();
        await expect(page.getByLabel("编辑王圻文档", { exact: true })).toBeVisible();
        await page.getByLabel("编辑王圻文档", { exact: true }).getByText("文件").click();
        await page.setInputFiles('input[type="file"]', {
            name: "new-wangqi.pdf",
            mimeType: "application/pdf",
            buffer: Buffer.from("source-bin")
        });
        await expect.poll(() => uploadRequests.at(-1)).toBe("POST");
        await page.getByLabel("编辑王圻文档", { exact: true }).getByText("版本").click();
        await page.getByTestId("wangqi-version-view-9001-button").click();
        await expect(page.getByText("历史：历史王圻文档")).toBeVisible();
        await page.getByTestId("wangqi-version-restore-9001-button").click();
        await page.getByRole("button", { name: /^\s*恢\s*复\s*$/ }).click();
        await expect
            .poll(() => resetRequests.at(-1))
            .toEqual({
                id: 1,
                versionId: 9001
            });
        await expect(page.getByRole("button", { name: /^\s*恢\s*复\s*$/ })).toBeHidden();
        await page
            .getByLabel("编辑王圻文档", { exact: true })
            .getByRole("button", { name: /^\s*取\s*消\s*$/ })
            .click();

        await page.getByRole("button", { name: "展开行操作" }).click();
        await page.getByRole("menuitem", { name: "删除" }).click();
        await page.getByRole("button", { name: /^\s*删\s*除\s*$/ }).click();
        await expect.poll(() => deleteRequests.at(-1)).toEqual({ id: 1 });
    });
});
