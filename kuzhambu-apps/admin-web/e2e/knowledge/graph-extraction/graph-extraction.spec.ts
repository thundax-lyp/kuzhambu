import { expect, test } from "@playwright/test";

test("loads graph extraction task material titles from the development server", async ({
    page
}) => {
    test.skip(process.env.KUZHAMBU_LIVE_E2E !== "true", "requires the local development server");

    await page.goto("/login");
    await page.getByPlaceholder("请输入后台账号").fill("developer");
    await page.getByPlaceholder("请输入密码").fill("Q1w2e3r$");
    await page.getByPlaceholder("验证码").fill("6666");
    const [loginResponse, permissionsResponse] = await Promise.all([
        page.waitForResponse(
            (response) =>
                response.url().includes("/kuzhambu-admin-api/api/auth/session/login") &&
                response.request().method() === "POST"
        ),
        page.waitForResponse(
            (response) =>
                response
                    .url()
                    .includes("/kuzhambu-admin-api/api/sys/current-user/permission/list") &&
                response.request().method() === "POST"
        ),
        page.getByRole("button", { name: /登\s*录/ }).click()
    ]);
    expect((await loginResponse.json()) as { code?: string }).toMatchObject({
        code: "COMMON-00000"
    });
    expect((await permissionsResponse.json()) as { code?: string }).toMatchObject({
        code: "COMMON-00000"
    });
    await expect(page).toHaveURL(/\/dashboard$/);

    const taskPageResponse = page.waitForResponse(
        (response) =>
            response.url().includes("/kuzhambu-admin-api/api/knowledge/graph/task/page") &&
            response.request().method() === "POST"
    );
    await page.goto("/knowledge/graph-extraction");
    await expect(page.getByRole("heading", { name: "知识抽取" })).toBeVisible();

    const response = await taskPageResponse;
    const payload = (await response.json()) as {
        data?: { records?: Array<{ materialTitle?: string }> };
    };
    const records = payload.data?.records ?? [];

    expect(records).not.toEqual([]);
    expect(records.every((task) => Boolean(task.materialTitle?.trim()))).toBeTruthy();
});
