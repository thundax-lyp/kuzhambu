import { postJson } from "@/api/http";
import type { OptionsRecord } from "@/types/options";
import type { RoleMenuNode, RoleRecord } from "./role-types";

export interface RoleQuery {
    enable?: boolean | null;
}

export interface RoleMenuCommand {
    id: string;
}

export interface RoleSaveCommand {
    id?: string | null;
    name?: string | null;
    admin?: boolean | null;
    enable?: boolean | null;
    remarks?: string | null;
    menus?: RoleMenuCommand[] | null;
}

export interface RoleStatusCommand {
    roles: Array<{
        id: string;
        enable?: boolean | null;
    }>;
}

export interface RoleSortCommand {
    orderedIds: string[];
    sortDirection?: "ASC" | "DESC";
}

export type RoleOptionKeys = "statusOptions" | "privilegeOptions";

export const list = (request: RoleQuery = {}) => {
    return postJson<RoleRecord[], RoleQuery>("/sys/role/list", {
        body: request
    });
};

export const listMenus = () => {
    return postJson<RoleMenuNode[]>("/sys/role/menu/tree");
};

export const getOptions = () => {
    return postJson<OptionsRecord<RoleOptionKeys>, Record<string, never>>("/sys/role/options", {
        body: {}
    });
};

export const create = (request: RoleSaveCommand) => {
    return postJson<RoleRecord, RoleSaveCommand>("/sys/role/create", {
        body: request
    });
};

export const changeInfo = (request: RoleSaveCommand) => {
    return postJson<RoleRecord, RoleSaveCommand>("/sys/role/update", {
        body: request
    });
};

export const changeStatus = (request: RoleStatusCommand) => {
    return postJson<boolean, Array<{ id: string; enable?: boolean | null }>>("/sys/role/enable", {
        body: request.roles
    });
};

export const sort = (request: RoleSortCommand) => {
    return postJson<boolean, RoleSortCommand>("/sys/role/sort", {
        body: request
    });
};

export const remove = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/role/delete", {
        body: ids.map((id) => ({ id }))
    });
};
