import { expect, test } from "@playwright/test";
import { mockGraphShell } from "./graph-mock.fixture";

test("renders the graph workbench as a read-only progressive atlas", async ({ page }) => {
    const { unexpectedBackendRequests, unexpectedConsoleErrors } = await mockGraphShell(page);

    await page.goto("/knowledge/graph-workbench");

    await expect(page.getByRole("heading", { name: "图谱工作台" })).toBeVisible();
    await expect(page.getByText("正式节点 2")).toBeVisible();
    await expect(page.getByText("结构缺口 1")).toBeVisible();
    await expect(
        page.getByRole("img", { name: /正式图画布：已展示 2 个节点和 102 条关系/ })
    ).toBeVisible();
    await expect(page.getByText("正式关系已发布")).toBeVisible();
    await expect(page.locator(".graph-workbench-page").getByRole("button")).toHaveCount(0);
    await expect(page.locator(".graph-workbench-page").getByRole("textbox")).toHaveCount(0);
    expect(unexpectedBackendRequests).toEqual([]);
    expect(unexpectedConsoleErrors).toEqual([]);
});
