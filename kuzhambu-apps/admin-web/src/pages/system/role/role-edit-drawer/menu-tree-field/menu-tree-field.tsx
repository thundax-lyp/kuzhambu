import { Tree } from "antd";
import type { DataNode } from "antd/es/tree";
import { useState, type Key } from "react";
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
    const [expandedKeys, setExpandedKeys] = useState<Key[]>(expandedMenuIds);

    return (
        <div className="menu-tree-field">
            <Tree
                className="menu-tree-field-control"
                classNames={{ itemTitle: "menu-tree-field-node-title" }}
                key={expandedMenuIds.join(",")}
                checkable
                disabled={loading}
                checkedKeys={value}
                expandedKeys={expandedKeys}
                treeData={treeData}
                selectable={false}
                onCheck={(keys) => onChange?.(Array.isArray(keys) ? keys : keys.checked)}
                onExpand={(keys) => {
                    setExpandedKeys(keys);
                }}
            />
        </div>
    );
};
