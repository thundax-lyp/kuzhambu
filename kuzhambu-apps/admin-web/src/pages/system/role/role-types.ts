export interface RoleMenuNode {
    id: string;
    parentId?: string | null;
    name: string;
    perms?: string | null;
}

export interface RoleMenuTreeNode extends RoleMenuNode {
    children?: RoleMenuTreeNode[];
}

export interface RoleRecord {
    id: string;
    name: string;
    admin?: boolean | null;
    enable?: boolean | null;
    remarks?: string | null;
    menus?: RoleMenuNode[] | null;
}
