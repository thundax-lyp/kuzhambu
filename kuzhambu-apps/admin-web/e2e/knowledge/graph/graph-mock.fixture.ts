import type { Page, Route } from "@playwright/test";

const PERMISSIONS = ["knowledge:graph:view", "knowledge:graph:edit", "knowledge:graph:apply"];

const fulfill = async (route: Route, data: unknown) =>
    route.fulfill({
        contentType: "application/json",
        body: JSON.stringify({ code: "COMMON-00000", message: "success", data })
    });

const edgeBatch = (start: number, count: number) => ({
    edges: Array.from({ length: count }, (_, index) => ({
        id: `edge-${start + index}`,
        relationType: "MENTIONS",
        sourceNodeId: "node-1",
        targetNodeId: "node-2"
    })),
    nodes: [
        { id: "node-1", name: "杜甫", nodeType: "PERSON" },
        { id: "node-2", name: "李白", nodeType: "PERSON" }
    ]
});

export const mockGraphShell = async (page: Page) => {
    const unexpectedBackendRequests: string[] = [];
    const unexpectedConsoleErrors: string[] = [];
    page.on("console", (message) => {
        if (message.type() === "error") {
            unexpectedConsoleErrors.push(message.text());
        }
    });
    page.on("pageerror", (error) => {
        unexpectedConsoleErrors.push(error.message);
    });
    await page.addInitScript(() => {
        localStorage.setItem("kuzhambu.admin.accessToken", "mock-token");
        localStorage.setItem("kuzhambu.admin.refreshToken", "mock-refresh");
        localStorage.setItem("kuzhambu.admin.accessTokenExpireAt", String(Date.now() + 3600000));
        localStorage.setItem(
            "kuzhambu.admin.permissions",
            JSON.stringify([
                "knowledge:graph:view",
                "knowledge:graph:edit",
                "knowledge:graph:apply"
            ])
        );
    });
    await page.route("**/kuzhambu-admin-api/**", async (route) => {
        unexpectedBackendRequests.push(route.request().url());
        await route.fulfill({ status: 404, contentType: "application/json", body: "{}" });
    });
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/get", (route) =>
        fulfill(route, {
            id: "user-1",
            loginName: "developer",
            name: "Developer",
            ranks: 9,
            admin: true,
            superAdmin: true
        })
    );
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/permission/list", (route) =>
        fulfill(route, { perms: PERMISSIONS })
    );
    await page.route("**/kuzhambu-admin-api/api/sys/current-user/menu/list", (route) =>
        fulfill(route, [
            { id: "dashboard", name: "仪表盘", url: "/dashboard" },
            { id: "knowledge", name: "知识治理" },
            {
                id: "knowledge-graph",
                parentId: "knowledge",
                name: "知识图谱",
                url: "/knowledge/graph"
            }
        ])
    );
    await page.route("**/kuzhambu-admin-api/api/auth/session/token/refresh", (route) =>
        fulfill(route, {
            token: "mock-token",
            refreshToken: "mock-refresh",
            expireAt: Date.now() + 3600000
        })
    );
    await page.route("**/kuzhambu-admin-api/api/auth/session/pre-auth-session/request", (route) =>
        fulfill(route, { loginToken: "mock-login-token", publicKey: "04mock-public-key" })
    );
    await page.route("**/kuzhambu-admin-api/api/knowledge/graph/workbench/overview/get", (route) =>
        fulfill(route, {
            coveredMaterialCount: "2",
            isolatedNodeCount: "0",
            missingCoreRelationNodeCount: "1",
            pendingConflictCount: "0",
            publishedEdgeCount: "102",
            publishedNodeCount: "2",
            recentActivities: [
                { occurredAt: "1724025600000", summary: "正式关系已发布", type: "PUBLISH" }
            ],
            snapshotAt: "1724025600000"
        })
    );
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph/workbench/recent-edges/list",
        (route) => fulfill(route, edgeBatch(1, 2))
    );
    let oneHopRequestCount = 0;
    await page.route(
        "**/kuzhambu-admin-api/api/knowledge/graph/workbench/one-hop-edges/list",
        (route) => {
            oneHopRequestCount += 1;
            return fulfill(route, {
                ...edgeBatch(oneHopRequestCount === 1 ? 3 : 53, 50),
                nextCursor: oneHopRequestCount === 1 ? "edge-52" : null,
                truncated: oneHopRequestCount === 1
            });
        }
    );
    return { unexpectedBackendRequests, unexpectedConsoleErrors };
};
