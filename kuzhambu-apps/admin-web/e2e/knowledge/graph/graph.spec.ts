import { expect, test } from "@playwright/test";
import { mockGraphShell } from "./graph-mock.fixture";

test.describe("knowledge graph mock flows", () => {
    test("covers workbench, material publication, governance confirmation and deletion retry", async ({
        page
    }) => {
        const { unexpectedBackendRequests, unexpectedConsoleErrors } = await mockGraphShell(page);

        await page.goto("/knowledge/graph-workbench");
        await expect(page.getByRole("heading", { name: "图谱工作台" })).toBeVisible();
        await expect(page.getByText("种子节点已淡化，等待边批次返回")).toBeVisible();
        await expect(page.getByText("边批次加载完成，已移除孤立节点")).toBeVisible();
        await expect(page.getByRole("button", { name: "查看节点 孤立节点" })).toHaveCount(0);
        await expect(page.getByLabel("局部画布节点数量")).toHaveText("当前节点 4 / 最多 200");

        await page.goto("/knowledge/graph-material");
        await expect(page.getByRole("heading", { name: "图谱素材库" })).toBeVisible();
        await page.getByTestId("knowledge-graph-material-select-material-draft-button").click();
        await page.getByTestId("knowledge-graph-material-select-material-failed-button").click();
        await page.getByTestId("knowledge-graph-material-open-batch-publication-button").click();
        await page.getByTestId("knowledge-graph-material-confirm-batch-publication-button").click();
        await expect(page.getByText("部分素材发布失败，其余结果已保留。")).toBeVisible();

        await page.getByTestId("knowledge-graph-material-close-batch-panel-button").click();
        await page.getByTestId("knowledge-graph-material-open-material-draft-button").click();
        await page.getByTestId("knowledge-graph-material-resolve-conflict-button").click();
        await page.getByTestId("knowledge-graph-material-publish-preview-button").click();
        await expect(page.getByText("发布已冻结")).toBeVisible();
        await page.getByTestId("knowledge-graph-material-withdraw-preview-button").click();
        await expect(page.getByText("素材已撤回")).toBeVisible();

        await page.goto("/knowledge/graph-governance");
        await expect(page.getByRole("heading", { name: "图谱治理" })).toBeVisible();
        await page.getByRole("button", { name: "合并变更" }).click();
        await page.getByRole("button", { name: "分配到李白" }).click();
        await page.getByRole("button", { name: "确认并应用合并" }).click();
        await expect(page.getByText("Mock 已应用合并变更")).toBeVisible();
        await page.getByRole("button", { name: "拆分变更" }).click();
        await expect(page.getByRole("button", { name: "确认并应用拆分" })).toBeDisabled();
        await page.getByRole("button", { name: "分配到李白" }).click();
        await page.getByRole("button", { name: "确认并应用拆分" }).click();
        await expect(page.getByText("Mock 已应用拆分变更")).toBeVisible();

        await page.goto("/knowledge/graph-deletion-changes");
        await page.getByRole("button", { name: "查看影响" }).click();
        await page.getByRole("button", { name: "撤回关联" }).click();
        await expect(page.getByText("已选择 WITHDRAW_ASSOCIATIONS")).toBeVisible();

        await page.goto("/knowledge/graph-deletion-tasks");
        await expect(page.getByText("删除关联时检测到并发发布，请重试。")).toBeVisible();
        await page.getByTestId("knowledge-graph-deletion-task-retry-button").click();
        await expect(
            page.getByTestId("knowledge-graph-deletion-task-detail-drawer").getByText("SUCCEEDED")
        ).toBeVisible();
        expect(unexpectedBackendRequests).toEqual([]);
        expect(unexpectedConsoleErrors).toEqual([]);
    });
});
