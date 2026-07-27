import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { CurrentUserRecord } from "@/service/current-user-types";
import type { UserRecord } from "@/pages/system/user/user-types";
import { UserEditDrawer } from "./user-edit-drawer";
import * as service from "@/pages/system/user/user-service";

vi.mock("@/pages/system/user/user-service", () => ({
    listRoles: vi.fn()
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
            <UserEditDrawer
                open
                title="编辑用户"
                saveText="保存"
                user={user}
                currentUser={currentUser}
                departments={[baseUser.department!]}
                onClose={vi.fn()}
                onSave={vi.fn()}
            />
        </QueryClientProvider>
    );
    return {
        ...view,
        rerenderDrawer: (nextUser: UserRecord) =>
            view.rerender(
                <QueryClientProvider client={queryClient}>
                    <UserEditDrawer
                        open
                        title="编辑用户"
                        saveText="保存"
                        user={nextUser}
                        currentUser={currentUser}
                        departments={[baseUser.department!]}
                        onClose={vi.fn()}
                        onSave={vi.fn()}
                    />
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
});
