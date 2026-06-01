import { postJson } from "@/api/http";
import type { DepartmentNode } from "./department-types";

export interface DepartmentListQuery {
    parentId?: string | null;
    name?: string | null;
    remarks?: string | null;
}

export interface DepartmentSaveCommand {
    id?: string | null;
    parentId?: string | null;
    name?: string | null;
    shortName?: string | null;
    remarks?: string | null;
}

export interface DepartmentMoveCommand {
    fromNodeId: string;
    toNodeId: string;
    type?: "after" | "before" | "inside" | "insideLast";
}

export const listDepartments = (request: DepartmentListQuery = {}) => {
    return postJson<DepartmentNode[], DepartmentListQuery>("/sys/department/list", {
        body: request
    });
};

export const addDepartment = (request: DepartmentSaveCommand) => {
    return postJson<DepartmentNode, DepartmentSaveCommand>("/sys/department/create", {
        body: request
    });
};

export const changeDepartmentInfo = (request: DepartmentSaveCommand) => {
    return postJson<DepartmentNode, DepartmentSaveCommand>("/sys/department/update", {
        body: request
    });
};

export const removeDepartments = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/department/delete", {
        body: ids.map((id) => ({ id }))
    });
};

export const moveDepartment = (request: DepartmentMoveCommand) => {
    return postJson<boolean, DepartmentMoveCommand>("/sys/department/move", {
        body: request
    });
};
