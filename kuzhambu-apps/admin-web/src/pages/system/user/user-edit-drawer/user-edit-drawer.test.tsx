import { QueryClient, QueryClientProvider, QueryObserver } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { App } from "antd";
import type { CurrentUserRecord } from "@/service/current-user-types";
import type { UserRecord } from "@/pages/system/user/user-types";
import { UserEditDrawer } from "./user-edit-drawer";
import * as service from "@/pages/system/user/user-service";

vi.mock("@/pages/system/user/user-service", () => ({
    changeInfo: vi.fn(),
    create: vi.fn(),
    listRoles: vi.fn(),
    uploadAvatar: vi.fn()
}));

const currentUser: CurrentUserRecord = {
    id: "current-user",
    loginName: "root",
    name: "Root",
    ranks: 9,
    superAdmin: true
};

const baseUser: UserRecord = {
    id: "user-1",
    loginName: "zhang.san",
    name: "张三",
    email: "zhang.san@example.test",
    mobile: "13800000000",
    avatar: "/avatar/old.png",
    ranks: 1,
    admin: false,
    enable: true,
    department: {
        id: "department-1",
        name: "编辑部"
    },
    roles: []
};

const renderDrawer = (user: UserRecord) => {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });
    const view = render(
        <QueryClientProvider client={queryClient}>
            <App>
                <UserEditDrawer
                    open
                    user={user}
                    currentUser={currentUser}
                    departments={[baseUser.department!]}
                    onClose={vi.fn()}
                />
            </App>
        </QueryClientProvider>
    );
    return {
        ...view,
        rerenderDrawer: (nextUser: UserRecord) =>
            view.rerender(
                <QueryClientProvider client={queryClient}>
                    <App>
                        <UserEditDrawer
                            open
                            user={nextUser}
                            currentUser={currentUser}
                            departments={[baseUser.department!]}
                            onClose={vi.fn()}
                        />
                    </App>
                </QueryClientProvider>
            )
    };
};

describe("UserEditDrawer", () => {
    beforeEach(() => {
        vi.mocked(service.listRoles).mockResolvedValue([]);
    });

    it("preserves unsaved fields when the same edited user refreshes after avatar upload", async () => {
        const user = userEvent.setup();
        const { rerenderDrawer } = renderDrawer(baseUser);

        const nameInput = await screen.findByLabelText("姓名");
        await user.clear(nameInput);
        await user.type(nameInput, "草稿姓名");

        rerenderDrawer({
            ...baseUser,
            name: "服务端姓名",
            avatar: "/avatar/new.png"
        });

        expect(screen.getByDisplayValue("草稿姓名")).toBeInTheDocument();
        expect(screen.queryByDisplayValue("服务端姓名")).not.toBeInTheDocument();
    });

    it("uses the refreshed active user page after avatar upload", async () => {
        const queryClient = new QueryClient({
            defaultOptions: { queries: { retry: false } }
        });
        const refreshedUser = { ...baseUser, avatar: "/avatar/new.png" };
        queryClient.setQueryData(["user", "page", { pageNo: 1 }], {
            records: [baseUser]
        });
        const activePageObserver = new QueryObserver(queryClient, {
            queryKey: ["user", "page", { pageNo: 2 }],
            queryFn: async () => ({ records: [refreshedUser] })
        });
        const unsubscribe = activePageObserver.subscribe(() => undefined);
        vi.mocked(service.uploadAvatar).mockResolvedValue(true);

        render(
            <QueryClientProvider client={queryClient}>
                <App>
                    <UserEditDrawer
                        open
                        user={baseUser}
                        currentUser={currentUser}
                        departments={[baseUser.department!]}
                        onClose={vi.fn()}
                    />
                </App>
            </QueryClientProvider>
        );

        const fileInput = document.querySelector('input[type="file"]');
        expect(fileInput).not.toBeNull();
        fireEvent.change(fileInput!, {
            target: { files: [new File(["avatar"], "avatar.png", { type: "image/png" })] }
        });

        await waitFor(() => expect(service.uploadAvatar).toHaveBeenCalledTimes(1));
        await waitFor(() => {
            expect(document.querySelector("img")?.getAttribute("src")).toContain("/avatar/new.png");
        });
        unsubscribe();
    });

    it("owns the edit mutation and closes after saving", async () => {
        const user = userEvent.setup();
        const onClose = vi.fn();
        vi.mocked(service.changeInfo).mockResolvedValue(baseUser);
        const queryClient = new QueryClient({
            defaultOptions: { queries: { retry: false } }
        });

        render(
            <QueryClientProvider client={queryClient}>
                <App>
                    <UserEditDrawer
                        open
                        user={baseUser}
                        currentUser={currentUser}
                        departments={[baseUser.department!]}
                        onClose={onClose}
                    />
                </App>
            </QueryClientProvider>
        );

        await screen.findByLabelText("姓名");
        await user.click(screen.getByRole("button", { name: /保\s*存/ }));

        await waitFor(() => expect(service.changeInfo).toHaveBeenCalledTimes(1));
        await waitFor(() => expect(onClose).toHaveBeenCalledTimes(1));
        expect(screen.queryByTestId("system-user-user-remove-button")).not.toBeInTheDocument();
    });
});
