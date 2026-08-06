import { Tree } from "antd";
import type { DataNode } from "antd/es/tree";
import type { Key } from "react";
import "./menu-tree-field.css";

interface MenuTreeFieldProps {
    value?: Key[];
    treeData: DataNode[];
    expandedMenuIds: Key[];
    loading?: boolean;
    onChange?: (value: Key[]) => void;
}

export const MenuTreeField = ({
    value = [],
    treeData,
    expandedMenuIds,
    loading,
    onChange
}: MenuTreeFieldProps) => {
    return (
        <div className="menu-tree-field">
            <Tree
                checkable
                disabled={loading}
                defaultExpandAll
                checkedKeys={value}
                defaultExpandedKeys={expandedMenuIds}
                treeData={treeData}
                selectable={false}
                onCheck={(keys) => onChange?.(Array.isArray(keys) ? keys : keys.checked)}
            />
        </div>
    );
};
