import { useMutation, useQuery } from "@tanstack/react-query";
import { App, Form, Input, Spin } from "antd";
import { KuzhambuForm, KuzhambuFormItem, KuzhambuModal } from "@/components";
import * as service from "../graph-governance-service";

export interface GovernanceDeleteTarget {
    id: string;
    lockVersion?: string | null;
    type: "NODE" | "EDGE";
}

interface GovernanceDeleteModalProps {
    onCancel: () => void;
    onDeleted: () => Promise<unknown>;
    target?: GovernanceDeleteTarget | null;
}

interface DeleteValues {
    reason: string;
}

export const GovernanceDeleteModal = ({
    onCancel,
    onDeleted,
    target
}: GovernanceDeleteModalProps) => {
    const { message } = App.useApp();
    const [form] = Form.useForm<DeleteValues>();
    const previewQuery = useQuery({
        enabled: Boolean(target),
        queryFn: () =>
            target?.type === "NODE"
                ? service.previewPublishedNodeDeletion({ nodeId: target.id })
                : service.previewPublishedRelationDeletion({ edgeId: target?.id ?? "" }),
        queryKey: ["knowledge", "graph-governance", "delete-preview", target?.type, target?.id]
    });
    const deleteMutation = useMutation({
        mutationFn: (values: DeleteValues) => {
            if (!target || !previewQuery.data) {
                throw new Error("删除影响尚未加载完成。");
            }
            return target.type === "NODE"
                ? service.deletePublishedNode({
                      impactToken: previewQuery.data.impactToken,
                      lockVersion: target.lockVersion ?? "",
                      nodeId: target.id,
                      reason: values.reason
                  })
                : service.deletePublishedRelation({
                      edgeId: target.id,
                      impactToken: previewQuery.data.impactToken,
                      lockVersion: target.lockVersion ?? "",
                      reason: values.reason
                  });
        },
        onError: (error) =>
            message.error(error instanceof Error ? error.message : "删除图谱条目失败"),
        onSuccess: async () => {
            message.success("发布图谱条目已删除");
            await onDeleted();
            onCancel();
        }
    });

    return (
        <KuzhambuModal
            confirmLoading={deleteMutation.isPending}
            okButtonProps={{ danger: true, disabled: !previewQuery.data?.executable }}
            okText="删除"
            open={Boolean(target)}
            testId="knowledge-graph-governance-delete-modal"
            title={target?.type === "NODE" ? "删除节点" : "删除关系"}
            onCancel={onCancel}
            onOk={() => form.submit()}
        >
            {previewQuery.isLoading ? <Spin /> : null}
            {previewQuery.data ? (
                <p>
                    将影响 {previewQuery.data.nodes.length} 个节点和{" "}
                    {previewQuery.data.edges.length} 条关系。
                </p>
            ) : null}
            <KuzhambuForm form={form} onFinish={(values) => deleteMutation.mutate(values)}>
                <KuzhambuFormItem
                    label="删除原因"
                    name="reason"
                    rules={[{ required: true, message: "请填写删除原因" }]}
                >
                    <Input.TextArea maxLength={1024} rows={3} />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuModal>
    );
};
