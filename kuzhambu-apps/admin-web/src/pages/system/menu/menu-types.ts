export interface MenuNode {
    id: string;
    parentId?: string | null;
    name: string;
    perms?: string | null;
    ranks?: number | null;
    display?: boolean | null;
    displayParams?: string | null;
    url?: string | null;
    remarks?: string | null;
}

export interface MenuTableNode extends MenuNode {
    children?: MenuTableNode[];
}
