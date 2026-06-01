import { postJson } from "@/api/http";
import type { MenuNode } from "./menu-types";

export interface MenuListQuery {
    parentId?: string | null;
    display?: boolean | null;
}

export interface MenuSaveCommand {
    id?: string | null;
    parentId?: string | null;
    name?: string | null;
    perms?: string | null;
    ranks?: number | null;
    display?: boolean | null;
    displayParams?: string | null;
    url?: string | null;
    remarks?: string | null;
}

export interface MenuMoveCommand {
    fromNodeId: string;
    toNodeId: string;
    type?: "after" | "before" | "inside" | "insideLast";
}

export const listMenus = (request: MenuListQuery = {}) => {
    return postJson<MenuNode[], MenuListQuery>("/sys/menu/list", {
        body: request
    });
};

export const addMenu = (request: MenuSaveCommand) => {
    return postJson<MenuNode, MenuSaveCommand>("/sys/menu/create", {
        body: request
    });
};

export const changeMenuInfo = (request: MenuSaveCommand) => {
    return postJson<MenuNode, MenuSaveCommand>("/sys/menu/update", {
        body: request
    });
};

export const removeMenus = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/menu/delete", {
        body: ids.map((id) => ({ id }))
    });
};

export const changeMenuDisplay = (id: string, display: boolean) => {
    return postJson<boolean, Array<{ id: string; display: boolean }>>("/sys/menu/display", {
        body: [{ id, display }]
    });
};

export const moveMenu = (request: MenuMoveCommand) => {
    return postJson<boolean, MenuMoveCommand>("/sys/menu/move", {
        body: request
    });
};
