export interface LogDepartmentRecord {
    id?: string | null;
    name?: string | null;
    namePath?: string | null;
}

export interface LogUserRecord {
    id?: string | null;
    loginName?: string | null;
    name?: string | null;
    department?: LogDepartmentRecord | null;
}

export interface LogRecord {
    id: string;
    remarks?: string | null;
    createDate?: string | null;
    type?: string | null;
    title?: string | null;
    remoteAddr?: string | null;
    userAgent?: string | null;
    method?: string | null;
    requestUri?: string | null;
    requestParams?: string | null;
    createUser?: LogUserRecord | null;
}
