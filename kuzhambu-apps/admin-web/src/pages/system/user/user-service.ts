import { postFormData, postJson } from "@/api/http";
import type { OptionsRecord } from "@/types/options";
import type { Page } from "@/types/page";
import type { UserDepartmentNode, UserRecord, UserRoleRecord } from "./user-types";

export interface PageQuery {
    pageNo?: number;
    pageSize?: number;
    departmentId?: string | null;
    loginName?: string | null;
    name?: string | null;
    enable?: boolean | null;
    orderBy?: string | null;
}

export interface StatusCommand {
    users: Array<{
        id: string;
        enable?: boolean | null;
    }>;
}

export interface SaveCommand {
    id?: string | null;
    remarks?: string | null;
    loginName?: string | null;
    loginPass?: string | null;
    token?: string | null;
    ranks?: number | null;
    name?: string | null;
    email?: string | null;
    mobile?: string | null;
    admin?: boolean | null;
    enable?: boolean | null;
    department?: { id: string } | null;
    roles?: Array<{ id: string }> | null;
}

export type UserOptionKeys = "statusOptions" | "rankOptions";

export const page = (request: PageQuery = {}) => {
    return postJson<Page<UserRecord>, PageQuery>("/sys/user/page", {
        body: request
    });
};

export const listDepartments = () => {
    return postJson<UserDepartmentNode[]>("/sys/user/department/list");
};

export const listRoles = () => {
    return postJson<UserRoleRecord[]>("/sys/user/role/list");
};

export const getOptions = () => {
    return postJson<OptionsRecord<UserOptionKeys>, Record<string, never>>(
        "/sys/user/options/list",
        {
            body: {}
        }
    );
};

export const changeStatus = (request: StatusCommand) => {
    return postJson<boolean, Array<{ id: string; enable?: boolean | null }>>(
        "/sys/user/status/update",
        {
            body: request.users
        }
    );
};

export const remove = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/user/delete", {
        body: ids.map((id) => ({ id }))
    });
};

export const create = (request: SaveCommand) => {
    return postJson<UserRecord, SaveCommand>("/sys/user/create", {
        body: request
    });
};

export const changeInfo = (request: SaveCommand) => {
    return postJson<UserRecord, SaveCommand>("/sys/user/update", {
        body: request
    });
};

export const uploadAvatar = (id: string, avatar: File) => {
    const body = new FormData();
    body.append("id", id);
    body.append("avatar", avatar);
    return postFormData<boolean>("/sys/user/avatar/upload", body);
};
