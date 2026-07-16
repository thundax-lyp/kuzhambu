import { Form, Input, Select } from "antd";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type {
    QualityAnnotationRecord,
    QualityAnnotationTarget,
    UpsertQualityAnnotationCommand
} from "../refinement-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

type QualityAnnotationFormValues = Pick<
    UpsertQualityAnnotationCommand,
    "annotationStatus" | "annotationLabel" | "comment"
>;

interface RefinementQualityAnnotationDrawerProps {
    deleting?: boolean;
    existingAnnotation?: QualityAnnotationRecord | null;
    open: boolean;
    saving?: boolean;
    target?: QualityAnnotationTarget | null;
    onCancel: () => void;
    onDelete: (annotation: QualityAnnotationRecord) => void;
    onSave: (values: QualityAnnotationFormValues) => void;
}

const statusOptions = [
    { label: "PASSED", value: "PASSED" },
    { label: "ISSUE", value: "ISSUE" },
    { label: "IGNORED", value: "IGNORED" }
];

const labelOptions = [
    { label: "MISSING_SOURCE", value: "MISSING_SOURCE" },
    { label: "WRONG_ENTITY", value: "WRONG_ENTITY" },
    { label: "WRONG_RELATION", value: "WRONG_RELATION" },
    { label: "INCOMPLETE_LINEAGE", value: "INCOMPLETE_LINEAGE" },
    { label: "DUPLICATED", value: "DUPLICATED" },
    { label: "OTHER", value: "OTHER" }
];

export const RefinementQualityAnnotationDrawer = ({
    deleting = false,
    existingAnnotation = null,
    open,
    saving = false,
    target = null,
    onCancel,
    onDelete,
    onSave
}: RefinementQualityAnnotationDrawerProps) => {
    const [form] = Form.useForm<QualityAnnotationFormValues>();

    return (
        <KuzhambuDrawer
            destroyOnClose
            open={open}
            size="small"
            title={`质量标注${target ? ` / ${target.objectType} / ${target.objectKey}` : ""}`}
            onClose={onCancel}
        >
            <Form<QualityAnnotationFormValues>
                form={form}
                initialValues={{
                    annotationStatus: existingAnnotation?.annotationStatus || "ISSUE",
                    annotationLabel: existingAnnotation?.annotationLabel || "OTHER",
                    comment: existingAnnotation?.comment || ""
                }}
                layout="vertical"
                onFinish={onSave}
            >
                <Form.Item
                    label="标注状态"
                    name="annotationStatus"
                    rules={[{ required: true, message: "请选择标注状态" }]}
                >
                    <Select options={statusOptions} />
                </Form.Item>
                <Form.Item
                    label="标注标签"
                    name="annotationLabel"
                    rules={[{ required: true, message: "请选择标注标签" }]}
                >
                    <Select options={labelOptions} />
                </Form.Item>
                <Form.Item label="备注" name="comment">
                    <Input.TextArea rows={5} />
                </Form.Item>
                <KuzhambuSpace size={8}>
                    <KuzhambuButton name="保存" htmlType="submit" loading={saving} type="primary">
                        保存
                    </KuzhambuButton>
                    <KuzhambuButton
                        name="删除标注"
                        danger
                        disabled={!existingAnnotation}
                        loading={deleting}
                        onClick={() => existingAnnotation && onDelete(existingAnnotation)}
                    >
                        删除标注
                    </KuzhambuButton>
                    <KuzhambuButton name="取消" onClick={onCancel}>
                        取消
                    </KuzhambuButton>
                </KuzhambuSpace>
            </Form>
        </KuzhambuDrawer>
    );
};
