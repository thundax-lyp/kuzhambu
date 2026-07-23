import { Form, Input, Select } from "antd";
import { useEffect } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuForm, KuzhambuFormItem } from "@/components/kuzhambu-form";
import type { DepartmentSaveCommand } from "../department-service";
import type { DepartmentNode } from "../department-types";

const { TextArea } = Input;

interface DepartmentEditDrawerProps {
    open?: boolean;
    department?: DepartmentNode | null;
    parentOptions: Array<{ label: string; value: string }>;
    saving?: boolean;
    onClose: () => void;
    onSave: (request: DepartmentSaveCommand) => void;
}

interface DepartmentFormValues {
    id?: string | null;
    parentId?: string | null;
    name: string;
    shortName?: string | null;
    remarks?: string | null;
}

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readFormRequest = (values: DepartmentFormValues): DepartmentSaveCommand => {
    return {
        id: values.id,
        parentId: values.parentId || null,
        name: values.name.trim(),
        shortName: normalizeSearch(values.shortName),
        remarks: normalizeSearch(values.remarks)
    };
};

const toFormValues = (department: DepartmentNode): DepartmentFormValues => {
    return {
        id: department.id,
        parentId: department.parentId || null,
        name: department.name,
        shortName: department.shortName,
        remarks: department.remarks
    };
};

export const DepartmentEditDrawer = ({
    open,
    department,
    parentOptions,
    saving,
    onClose,
    onSave
}: DepartmentEditDrawerProps) => {
    const [form] = Form.useForm<DepartmentFormValues>();

    useEffect(() => {
        if (!open) {
            return;
        }
        if (department) {
            form.setFieldsValue(toFormValues(department));
            return;
        }
        form.resetFields();
    }, [department, form, open]);

    const saveDepartment = async () => {
        const values = await form.validateFields();
        onSave(readFormRequest(values));
    };

    return (
        <KuzhambuDrawer
            testId="system-department-department-edit-drawer"
            className="department-edit-drawer"
            title={department ? "编辑部门" : "新增部门"}
            open={Boolean(open)}
            size="small"
            onClose={onClose}
            footerActions={[
                {
                    testId: "system-department-department-cancel-button",
                    title: "取消",
                    action: onClose
                },
                {
                    testId: "system-department-department-save-button",
                    title: "保存",
                    type: "primary",
                    loading: saving,
                    action: saveDepartment
                }
            ]}
        >
            <KuzhambuForm<DepartmentFormValues> form={form} className="department-edit-drawer-form">
                <Form.Item name="id" hidden>
                    <Input />
                </Form.Item>
                <KuzhambuFormItem name="parentId" label="上级部门" layoutSize="large">
                    <Select allowClear placeholder="不选择则作为根部门" options={parentOptions} />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    name="name"
                    label="部门名称"
                    rules={[{ required: true, message: "请输入部门名称" }]}
                >
                    <Input placeholder="例如：研发中心" />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="shortName" label="简称">
                    <Input placeholder="例如：R&D" />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="remarks" label="备注" layoutSize="large">
                    <TextArea rows={4} maxLength={200} showCount placeholder="部门职责说明" />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuDrawer>
    );
};
