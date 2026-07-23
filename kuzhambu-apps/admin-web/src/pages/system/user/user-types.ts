export interface UserDepartmentNode {
    id: string;
    parentId?: string | null;
    name: string;
    shortName?: string | null;
    namePath?: string | null;
}

export interface UserRoleRecord {
    id: string;
    name: string;
}

export interface UserRecord {
    id: string;
    remarks?: string | null;
    loginName?: string | null;
    ranks?: number | null;
    name: string;
    email?: string | null;
    mobile?: string | null;
    avatar?: string | null;
    superAdmin?: boolean | null;
    admin?: boolean | null;
    enable?: boolean | null;
    department?: UserDepartmentNode | null;
    roles?: UserRoleRecord[] | null;
}
