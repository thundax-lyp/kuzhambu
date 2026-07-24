import { Form, Input } from "antd";
import type { DataNode } from "antd/es/tree";
import { useEffect } from "react";
import type { Key } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import {
    KuzhambuForm,
    KuzhambuFormHiddenItem,
    KuzhambuFormItem,
    KuzhambuFormPlaceholderItem
} from "@/components/kuzhambu-form";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import { MenuTreeField } from "@/pages/system/role/components/menu-tree-field";
import type { RoleSaveCommand } from "@/pages/system/role/role-service";
import type { RoleRecord } from "@/pages/system/role/role-types";
import type { OptionsRecord } from "@/types/options";
import "./role-edit-drawer.css";

const { TextArea } = Input;

interface RoleEditDrawerProps {
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
    menus?: Key[];
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readFormRequest = (values: RoleFormValues): RoleSaveCommand => {
    return {
        id: values.id,
        name: values.name.trim(),
        admin: Boolean(values.admin),
        enable: values.enable !== false,
        remarks: normalizeText(values.remarks),
        menus: (values.menus || []).map(String).map((id) => ({ id }))
    };
};

const toFormValues = (role: RoleRecord): RoleFormValues => {
    return {
        id: role.id,
        name: role.name,
        admin: Boolean(role.admin),
        enable: role.enable !== false,
        remarks: role.remarks,
        menus: (role.menus || []).map((menu) => menu.id)
    };
};

const readOptionLabel = (
    options: OptionsRecord[string] | undefined,
    value: string,
    fallbackLabel: string
) => {
    return options?.find((option) => option.value === value)?.label || fallbackLabel;
};

export const RoleEditDrawer = ({
    open,
    role,
    treeData,
    expandedMenuIds,
    statusOptions,
    privilegeOptions,
    saving,
    onClose,
    onSave
}: RoleEditDrawerProps) => {
    const [form] = Form.useForm<RoleFormValues>();

    useEffect(() => {
        if (!open) {
            return;
        }
        if (role) {
            form.setFieldsValue(toFormValues(role));
            return;
        }
        form.resetFields();
        form.setFieldsValue({ admin: false, enable: true, menus: [] });
    }, [form, open, role]);

    const saveRole = async () => {
        const values = await form.validateFields();
        onSave(readFormRequest(values));
    };

    return (
        <KuzhambuDrawer
            testId="system-role-role-edit-drawer"
            className="role-edit-drawer"
            title={role ? "编辑角色" : "新增角色"}
            open={Boolean(open)}
            size="large"
            onClose={onClose}
            footerActions={[
                { testId: "system-role-role-cancel-button", title: "取消", action: onClose },
                {
                    testId: "system-role-role-save-button",
                    title: "保存",
                    type: "primary",
                    loading: saving,
                    action: saveRole
                }
            ]}
        >
            <KuzhambuForm<RoleFormValues> form={form} className="system-role-edit-drawer-form">
                <KuzhambuFormHiddenItem name="id">
                    <Input />
                </KuzhambuFormHiddenItem>
                <KuzhambuFormItem
                    name="name"
                    label="角色名称"
                    layoutSize="middle"
                    rules={[{ required: true, message: "请输入角色名称" }]}
                >
                    <Input placeholder="例如：运营管理员" />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem />
                <KuzhambuFormItem name="admin" label="管理权限" valuePropName="checked">
                    <KuzhambuSwitch
                        checkedChildren={readOptionLabel(privilegeOptions, "ADMIN", "管理员角色")}
                        unCheckedChildren={readOptionLabel(privilegeOptions, "NORMAL", "普通角色")}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem />
                <KuzhambuFormItem name="enable" label="角色状态" valuePropName="checked">
                    <KuzhambuSwitch
                        checkedChildren={readOptionLabel(statusOptions, "ENABLED", "启用")}
                        unCheckedChildren={readOptionLabel(statusOptions, "DISABLED", "禁用")}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="menus" label="菜单权限" layoutSize="large">
                    <MenuTreeField treeData={treeData} expandedMenuIds={expandedMenuIds} />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="remarks" label="备注" layoutSize="large">
                    <TextArea rows={3} maxLength={200} showCount placeholder="角色说明" />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuDrawer>
    );
};
