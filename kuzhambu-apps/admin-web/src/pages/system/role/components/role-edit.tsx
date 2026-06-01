import { Button, Form, Input, Tree, Typography } from "antd";
import type { DataNode } from "antd/es/tree";
import { useEffect, useState } from "react";
import type { Key } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import type { OptionsRecord } from "@/types/options";
import type { RoleSaveCommand } from "../role-service";
import type { RoleRecord } from "../role-types";

const { Text } = Typography;
const { TextArea } = Input;

interface RoleEditProps {
    open?: boolean;
    role?: RoleRecord | null;
    treeData: DataNode[];
    expandedMenuIds: Key[];
    statusOptions?: OptionsRecord[string];
    privilegeOptions?: OptionsRecord[string];
    saving?: boolean;
    onClose: () => void;
    onSave: (request: RoleSaveCommand) => void;
}

interface RoleFormValues {
    id?: string | null;
    name: string;
    admin?: boolean | null;
    enable?: boolean | null;
    remarks?: string | null;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readFormRequest = (values: RoleFormValues, checkedMenuKeys: Key[]): RoleSaveCommand => {
    return {
        id: values.id,
        name: values.name.trim(),
        admin: Boolean(values.admin),
        enable: values.enable !== false,
        remarks: normalizeText(values.remarks),
        menus: checkedMenuKeys.map(String).map((id) => ({ id }))
    };
};

const toFormValues = (role: RoleRecord): RoleFormValues => {
    return {
        id: role.id,
        name: role.name,
        admin: Boolean(role.admin),
        enable: role.enable !== false,
        remarks: role.remarks
    };
};

const readOptionLabel = (
    options: OptionsRecord[string] | undefined,
    value: string,
    fallbackLabel: string
) => {
    return options?.find((option) => option.value === value)?.label || fallbackLabel;
};

export const RoleEdit = ({
    open,
    role,
    treeData,
    expandedMenuIds,
    statusOptions,
    privilegeOptions,
    saving,
    onClose,
    onSave
}: RoleEditProps) => {
    const [form] = Form.useForm<RoleFormValues>();
    const [checkedMenuKeys, setCheckedMenuKeys] = useState<Key[]>(
        (role?.menus || []).map((menu) => menu.id)
    );

    useEffect(() => {
        if (!open) {
            return;
        }
        if (role) {
            form.setFieldsValue(toFormValues(role));
            return;
        }
        form.resetFields();
        form.setFieldsValue({ admin: false, enable: true });
    }, [form, open, role]);

    const saveRole = async () => {
        const values = await form.validateFields();
        onSave(readFormRequest(values, checkedMenuKeys));
    };

    return (
        <KuzhambuDrawer
            className="role-edit-drawer"
            title={role ? "编辑角色" : "新增角色"}
            open={Boolean(open)}
            size="middle"
            onClose={onClose}
            footer={
                <div className="role-edit-footer">
                    <Button onClick={onClose}>取消</Button>
                    <Button type="primary" loading={saving} onClick={saveRole}>
                        保存
                    </Button>
                </div>
            }
        >
            <Form<RoleFormValues> form={form} layout="vertical" className="role-editor-form">
                <Form.Item name="id" hidden>
                    <Input />
                </Form.Item>
                <Form.Item
                    name="name"
                    label="角色名称"
                    rules={[{ required: true, message: "请输入角色名称" }]}
                >
                    <Input placeholder="例如：运营管理员" />
                </Form.Item>
                <div className="role-editor-switches">
                    <Form.Item name="admin" label="管理权限" valuePropName="checked">
                        <KuzhambuSwitch
                            checkedChildren={readOptionLabel(
                                privilegeOptions,
                                "ADMIN",
                                "管理员角色"
                            )}
                            unCheckedChildren={readOptionLabel(
                                privilegeOptions,
                                "NORMAL",
                                "普通角色"
                            )}
                        />
                    </Form.Item>
                    <Form.Item name="enable" label="角色状态" valuePropName="checked">
                        <KuzhambuSwitch
                            checkedChildren={readOptionLabel(statusOptions, "ENABLED", "启用")}
                            unCheckedChildren={readOptionLabel(statusOptions, "DISABLED", "禁用")}
                        />
                    </Form.Item>
                </div>
                <Form.Item name="remarks" label="备注">
                    <TextArea rows={3} maxLength={200} showCount placeholder="角色说明" />
                </Form.Item>
                <div className="role-menu-panel">
                    <div className="role-menu-panel-head">
                        <Text strong>菜单权限</Text>
                        <Text type="secondary">{checkedMenuKeys.length} 项已选</Text>
                    </div>
                    <Tree
                        checkable
                        defaultExpandAll
                        checkedKeys={checkedMenuKeys}
                        defaultExpandedKeys={expandedMenuIds}
                        treeData={treeData}
                        selectable={false}
                        onCheck={(keys) =>
                            setCheckedMenuKeys(Array.isArray(keys) ? keys : keys.checked)
                        }
                    />
                </div>
            </Form>
        </KuzhambuDrawer>
    );
};
