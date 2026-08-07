import { ApartmentOutlined } from "@ant-design/icons";
import { Tree, Typography } from "antd";
import type { DataNode } from "antd/es/tree";
import { useMemo } from "react";
import type { Key } from "react";
import { KuzhambuAlert, KuzhambuButton, KuzhambuSpace } from "@/components";
import type { UserDepartmentNode } from "@/pages/system/user/user-types";
import "./user-department-tree.css";

const { Text } = Typography;

export const ALL_DEPARTMENT_ID = "all";

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
    departments: UserDepartmentNode[];
    error?: Error | null;
    loading: boolean;
    selectedDepartmentId: string;
    onRetry: () => void;
    onSelectDepartment: (departmentId: string) => void;
}

export const UserDepartmentTree = ({
    departments,
    error,
    loading,
    selectedDepartmentId,
    onRetry,
    onSelectDepartment
}: UserDepartmentTreeProps) => {
    const departmentTreeData = useMemo(() => buildDepartmentTree(departments), [departments]);
    const departmentTreeKeys = useMemo(
        () => collectTreeKeys(departmentTreeData),
        [departmentTreeData]
    );

    const selectDepartment = (keys: Key[]) => {
        onSelectDepartment(String(keys[0] || ALL_DEPARTMENT_ID));
    };

    return (
        <div className="user-department-panel">
            <div className="user-department-panel-head">
                <KuzhambuSpace size={8}>
                    <ApartmentOutlined />
                    <Text strong>部门</Text>
                </KuzhambuSpace>
            </div>
            {error ? (
                <KuzhambuAlert
                    showIcon
                    type="error"
                    title="部门加载失败"
                    description={error.message || "请稍后重试"}
                    action={
                        <KuzhambuButton
                            ariaLabel="重试加载部门"
                            testId="system-user-department-retry-button"
                            onClick={onRetry}
                        >
                            重试
                        </KuzhambuButton>
                    }
                />
            ) : null}
            {departments.length > 0 || !error ? (
                <Tree
                    className="user-department-tree-control"
                    classNames={{
                        item: "user-department-tree-node",
                        itemSwitcher: "user-department-tree-switcher",
                        itemTitle: "user-department-tree-title"
                    }}
                    key={departmentTreeKeys.join(",")}
                    blockNode
                    disabled={loading}
                    defaultExpandedKeys={departmentTreeKeys}
                    selectedKeys={[selectedDepartmentId]}
                    treeData={departmentTreeData}
                    onSelect={selectDepartment}
                />
            ) : null}
        </div>
    );
};
