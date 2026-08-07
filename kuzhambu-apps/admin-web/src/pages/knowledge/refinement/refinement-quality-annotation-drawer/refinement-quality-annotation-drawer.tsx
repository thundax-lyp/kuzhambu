import { Form, Input } from "antd";
import {
    KuzhambuDrawer,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuSpace,
    KuzhambuButton,
    KuzhambuSelect
} from "@/components";

import type { UpsertQualityAnnotationCommand } from "../refinement-service";
import type { QualityAnnotationRecord, QualityAnnotationTarget } from "../refinement-types";

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
            testId="knowledge-refinement-refinement-quality-annotation-drawer"
            destroyOnHidden
            forceRender
            open={open}
            size="small"
            title={`质量标注${target ? ` / ${target.objectType} / ${target.objectKey}` : ""}`}
            onClose={onCancel}
        >
            <KuzhambuForm<QualityAnnotationFormValues>
                form={form}
                initialValues={{
                    annotationStatus: existingAnnotation?.annotationStatus || "ISSUE",
                    annotationLabel: existingAnnotation?.annotationLabel || "OTHER",
                    comment: existingAnnotation?.comment || ""
                }}
                onFinish={onSave}
            >
                <KuzhambuFormItem
                    label="标注状态"
                    name="annotationStatus"
                    rules={[{ required: true, message: "请选择标注状态" }]}
                >
                    <KuzhambuSelect options={statusOptions} />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    label="标注标签"
                    name="annotationLabel"
                    rules={[{ required: true, message: "请选择标注标签" }]}
                >
                    <KuzhambuSelect options={labelOptions} />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="备注" name="comment" layoutSize="large">
                    <Input.TextArea rows={5} />
                </KuzhambuFormItem>
            </KuzhambuForm>
            <KuzhambuSpace size={8}>
                <KuzhambuButton
                    testId="knowledge-refinement-refinement-quality-annotation-save-button"
                    loading={saving}
                    type="primary"
                    onClick={() => form.submit()}
                >
                    保存
                </KuzhambuButton>
                <KuzhambuButton
                    testId="knowledge-refinement-refinement-quality-annotation-action-button"
                    danger
                    disabled={!existingAnnotation}
                    loading={deleting}
                    onClick={() => existingAnnotation && onDelete(existingAnnotation)}
                >
                    删除标注
                </KuzhambuButton>
                <KuzhambuButton
                    testId="knowledge-refinement-refinement-quality-annotation-cancel-button"
                    onClick={onCancel}
                >
                    取消
                </KuzhambuButton>
            </KuzhambuSpace>
        </KuzhambuDrawer>
    );
};
