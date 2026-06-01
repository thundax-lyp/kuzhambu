import { Button, Form, Input, InputNumber, Select } from "antd";
import { useEffect } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { MenuSaveCommand } from "../menu-service";
import type { MenuNode } from "../menu-types";

const { TextArea } = Input;

interface MenuEditProps {
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

export const MenuEdit = ({ open, menu, parentOptions, saving, onClose, onSave }: MenuEditProps) => {
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
            className="menu-edit-drawer"
            title={menu ? "编辑菜单" : "新增菜单"}
            open={Boolean(open)}
            size="small"
            onClose={onClose}
            footer={
                <div className="menu-edit-footer">
                    <Button onClick={onClose}>取消</Button>
                    <Button type="primary" loading={saving} onClick={saveMenu}>
                        保存
                    </Button>
                </div>
            }
        >
            <Form<MenuFormValues> form={form} layout="vertical" className="menu-editor-form">
                <Form.Item name="id" hidden>
                    <Input />
                </Form.Item>
                <Form.Item name="parentId" label="上级菜单">
                    <Select allowClear placeholder="不选择则作为根菜单" options={parentOptions} />
                </Form.Item>
                <Form.Item
                    name="name"
                    label="菜单名称"
                    rules={[{ required: true, message: "请输入菜单名称" }]}
                >
                    <Input placeholder="例如：菜单管理" />
                </Form.Item>
                <Form.Item name="url" label="URL">
                    <Input placeholder="例如：/system/menus" />
                </Form.Item>
                <Form.Item name="perms" label="权限标识">
                    <Input placeholder="例如：sys:menu:view" />
                </Form.Item>
                <Form.Item name="ranks" label="等级">
                    <InputNumber min={0} max={9} precision={0} className="menu-rank-input" />
                </Form.Item>
                <Form.Item name="display" label="显示状态">
                    <Select
                        options={[
                            { label: "显示", value: true },
                            { label: "隐藏", value: false }
                        ]}
                    />
                </Form.Item>
                <Form.Item name="displayParams" label="显示参数">
                    <TextArea
                        rows={3}
                        maxLength={1000}
                        showCount
                        placeholder='例如：{"icon":"menu"}'
                    />
                </Form.Item>
                <Form.Item name="remarks" label="备注">
                    <TextArea rows={3} maxLength={200} showCount placeholder="菜单说明" />
                </Form.Item>
            </Form>
        </KuzhambuDrawer>
    );
};
