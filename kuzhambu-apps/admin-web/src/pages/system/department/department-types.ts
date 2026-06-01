export interface DepartmentNode {
    id: string;
    parentId?: string | null;
    name: string;
    shortName?: string | null;
    namePath?: string | null;
    remarks?: string | null;
}

export interface DepartmentTableNode extends DepartmentNode {
    children?: DepartmentTableNode[];
}
