export interface CurrentUserRecord {
    id: string;
    loginName: string;
    ranks?: number | null;
    name?: string | null;
    email?: string | null;
    mobile?: string | null;
    avatar?: string | null;
    admin?: boolean | null;
    superAdmin?: boolean | null;
}

export interface CurrentUserMenuNode {
    id: string;
    parentId?: string | null;
    name: string;
    url?: string | null;
    icon?: string | null;
    displayParams?: string | null;
}

export interface CurrentUserPermsRecord {
    perms?: string[] | null;
}
