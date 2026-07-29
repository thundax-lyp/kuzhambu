import { Form, Input, InputNumber } from "antd";
import { useEffect } from "react";
import {
    KuzhambuDrawer,
    KuzhambuForm,
    KuzhambuFormHiddenItem,
    KuzhambuFormItem,
    KuzhambuFormPlaceholderItem,
    KuzhambuSelect,
    KuzhambuSwitch
} from "@/components";

import type { MenuSaveCommand } from "./menu-service";
import type { MenuNode } from "./menu-types";

const { TextArea } = Input;

interface MenuEditDrawerProps {
    open?: boolean;
    menu?: MenuNode | null;
    parentOptions: Array<{ label: string; value: string }>;
    saving?: boolean;
    onClose: () => void;
    onSave: (request: MenuSaveCommand) => void;
}

interface MenuFormValues {
    id?: string | null;
    parentId?: string | null;
    name: string;
    perms?: string | null;
    ranks?: number | null;
    display?: boolean | null;
    displayParams?: string | null;
    url?: string | null;
    remarks?: string | null;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readFormRequest = (values: MenuFormValues): MenuSaveCommand => {
    return {
        id: values.id,
        parentId: values.parentId || null,
        name: values.name.trim(),
        perms: normalizeText(values.perms),
        ranks: values.ranks,
        display: values.display !== false,
        displayParams: normalizeText(values.displayParams),
        url: normalizeText(values.url),
        remarks: normalizeText(values.remarks)
    };
};

const toFormValues = (menu: MenuNode): MenuFormValues => {
    return {
        id: menu.id,
        parentId: menu.parentId || null,
        name: menu.name,
        perms: menu.perms,
        ranks: menu.ranks,
        display: menu.display !== false,
        displayParams: menu.displayParams,
        url: menu.url,
        remarks: menu.remarks
    };
};

export const MenuEditDrawer = ({
    open,
    menu,
    parentOptions,
    saving,
    onClose,
    onSave
}: MenuEditDrawerProps) => {
    const [form] = Form.useForm<MenuFormValues>();

    useEffect(() => {
        if (!open) {
            return;
        }
        if (menu) {
            form.setFieldsValue(toFormValues(menu));
            return;
        }
        form.resetFields();
        form.setFieldsValue({ display: true, ranks: 0 });
    }, [form, menu, open]);

    const saveMenu = async () => {
        const values = await form.validateFields();
        onSave(readFormRequest(values));
    };

    return (
        <KuzhambuDrawer
            testId="system-menu-menu-edit-drawer"
            className="menu-edit-drawer"
            title={menu ? "编辑菜单" : "新增菜单"}
            open={Boolean(open)}
            size="large"
            onClose={onClose}
            footerActions={[
                { testId: "system-menu-menu-cancel-button", title: "取消", action: onClose },
                {
                    testId: "system-menu-menu-save-button",
                    title: "保存",
                    type: "primary",
                    loading: saving,
                    action: saveMenu
                }
            ]}
        >
            <KuzhambuForm<MenuFormValues> form={form} className="menu-edit-drawer-form">
                <KuzhambuFormHiddenItem name="id">
                    <Input />
                </KuzhambuFormHiddenItem>
                <KuzhambuFormItem name="parentId" label="上级菜单" layoutSize="middle">
                    <KuzhambuSelect
                        allowClear
                        placeholder="不选择则作为根菜单"
                        options={parentOptions}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem />
                <KuzhambuFormItem
                    name="name"
                    label="菜单名称"
                    rules={[{ required: true, message: "请输入菜单名称" }]}
                >
                    <Input placeholder="例如：菜单管理" />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem />
                <KuzhambuFormItem name="url" label="URL">
                    <Input placeholder="例如：/system/menus" />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="perms" label="权限标识">
                    <Input placeholder="例如：sys:menu:view" />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="ranks" label="等级">
                    <InputNumber min={0} max={9} precision={0} className="menu-rank-input" />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem />
                <KuzhambuFormItem name="display" label="显示状态" valuePropName="checked">
                    <KuzhambuSwitch checkedChildren="显示" unCheckedChildren="隐藏" />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="displayParams" label="显示参数" layoutSize="large">
                    <TextArea
                        rows={3}
                        maxLength={1000}
                        showCount
                        placeholder='例如：{"icon":"menu"}'
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="remarks" label="备注" layoutSize="large">
                    <TextArea rows={3} maxLength={200} showCount placeholder="菜单说明" />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuDrawer>
    );
};
