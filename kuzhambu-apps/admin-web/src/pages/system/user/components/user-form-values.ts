export interface UserFormValues {
    admin: boolean;
    departmentId?: string | null;
    email?: string | null;
    enable: boolean;
    loginName: string;
    loginPass: string;
    mobile?: string | null;
    name: string;
    ranks: number;
    roleIds: string[];
}
