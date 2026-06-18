import { ApartmentOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Space, Tree, Typography } from "antd";
import type { DataNode } from "antd/es/tree";
import { useEffect, useMemo } from "react";
import type { Key } from "react";
import * as service from "../user-service";
import type { UserDepartmentNode } from "../user-types";

const { Text } = Typography;

export const ALL_DEPARTMENT_ID = "all";

const EMPTY_DEPARTMENTS: UserDepartmentNode[] = [];

const buildDepartmentTree = (departments: UserDepartmentNode[]): DataNode[] => {
    const rootDepartment: UserDepartmentNode = {
        id: ALL_DEPARTMENT_ID,
        parentId: null,
        name: "全部部门",
        shortName: "全部"
    };
    const allDepartments = [rootDepartment, ...departments];
    const childrenByParentId = new Map<string | null | undefined, UserDepartmentNode[]>();
    allDepartments.forEach((department) => {
        const parentId =
            department.parentId || (department.id === ALL_DEPARTMENT_ID ? null : ALL_DEPARTMENT_ID);
        const children = childrenByParentId.get(parentId) || [];
        children.push(department);
        childrenByParentId.set(parentId, children);
    });

    const toNode = (department: UserDepartmentNode): DataNode => ({
        key: department.id,
        title: (
            <span className="user-department-node">
                <span>{department.name}</span>
            </span>
        ),
        children: childrenByParentId.get(department.id)?.map(toNode)
    });

    return (childrenByParentId.get(null) || []).map(toNode);
};

const collectTreeKeys = (nodes: DataNode[]): Key[] => {
    return nodes.flatMap((node) => [
        node.key,
        ...(node.children ? collectTreeKeys(node.children) : [])
    ]);
};

interface UserDepartmentTreeProps {
    refreshSignal: number;
    selectedDepartmentId: string;
    onDepartmentsChange: (departments: UserDepartmentNode[]) => void;
    onFetchingChange: (isFetching: boolean) => void;
    onSelectDepartment: (departmentId: string) => void;
}

export const UserDepartmentTree = ({
    refreshSignal,
    selectedDepartmentId,
    onDepartmentsChange,
    onFetchingChange,
    onSelectDepartment
}: UserDepartmentTreeProps) => {
    const departmentQuery = useQuery({
        queryKey: ["user", "department", "tree"],
        queryFn: () => service.listDepartments(),
        retry: false
    });
    const { data: departmentData, isFetching, refetch } = departmentQuery;
    const departments = useMemo(
        () => departmentData ?? EMPTY_DEPARTMENTS,
        [departmentData]
    );
    const departmentTreeData = useMemo(() => buildDepartmentTree(departments), [departments]);
    const departmentTreeKeys = useMemo(
        () => collectTreeKeys(departmentTreeData),
        [departmentTreeData]
    );

    useEffect(() => {
        onDepartmentsChange(departments);
    }, [departments, onDepartmentsChange]);

    useEffect(() => {
        onFetchingChange(isFetching);
    }, [isFetching, onFetchingChange]);

    useEffect(() => {
        if (refreshSignal > 0) {
            refetch();
        }
    }, [refetch, refreshSignal]);

    const selectDepartment = (keys: Key[]) => {
        onSelectDepartment(String(keys[0] || ALL_DEPARTMENT_ID));
    };

    return (
        <div className="user-department-panel">
            <div className="user-department-panel-head">
                <Space size={8}>
                    <ApartmentOutlined />
                    <Text strong>部门</Text>
                </Space>
            </div>
            <Tree
                key={departmentTreeKeys.join(",")}
                blockNode
                defaultExpandedKeys={departmentTreeKeys}
                selectedKeys={[selectedDepartmentId]}
                treeData={departmentTreeData}
                onSelect={selectDepartment}
            />
        </div>
    );
};
