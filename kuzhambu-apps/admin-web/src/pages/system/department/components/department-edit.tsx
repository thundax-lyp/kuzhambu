import { Form, Input, Select } from "antd";
import { useEffect } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { DepartmentSaveCommand } from "../department-service";
import type { DepartmentNode } from "../department-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { TextArea } = Input;

interface DepartmentEditProps {
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

export const DepartmentEdit = ({
    open,
    department,
    parentOptions,
    saving,
    onClose,
    onSave
}: DepartmentEditProps) => {
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
            className="department-edit-drawer"
            title={department ? "编辑部门" : "新增部门"}
            open={Boolean(open)}
            size="small"
            onClose={onClose}
            footer={
                <div className="department-edit-footer">
                    <KuzhambuButton name="取消" onClick={onClose}>
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        name="保存"
                        type="primary"
                        loading={saving}
                        onClick={saveDepartment}
                    >
                        保存
                    </KuzhambuButton>
                </div>
            }
        >
            <Form<DepartmentFormValues>
                form={form}
                layout="vertical"
                className="department-editor-form"
            >
                <Form.Item name="id" hidden>
                    <Input />
                </Form.Item>
                <Form.Item name="parentId" label="上级部门">
                    <Select allowClear placeholder="不选择则作为根部门" options={parentOptions} />
                </Form.Item>
                <Form.Item
                    name="name"
                    label="部门名称"
                    rules={[{ required: true, message: "请输入部门名称" }]}
                >
                    <Input placeholder="例如：研发中心" />
                </Form.Item>
                <Form.Item name="shortName" label="简称">
                    <Input placeholder="例如：R&D" />
                </Form.Item>
                <Form.Item name="remarks" label="备注">
                    <TextArea rows={4} maxLength={200} showCount placeholder="部门职责说明" />
                </Form.Item>
            </Form>
        </KuzhambuDrawer>
    );
};
