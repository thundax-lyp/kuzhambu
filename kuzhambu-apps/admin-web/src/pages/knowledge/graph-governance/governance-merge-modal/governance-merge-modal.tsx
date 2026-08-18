import { useMutation, useQuery } from "@tanstack/react-query";
import { App, Form, Input } from "antd";
import { useState } from "react";
import {
    KuzhambuButton,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuModal,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import type { GraphGovernanceNodeRecord } from "../graph-governance-types";
import * as service from "../graph-governance-service";
import { PublishedNodeRemoteSelect } from "../governance-editor-modal/published-node-remote-select";

interface GovernanceMergeModalProps {
    node?: GraphGovernanceNodeRecord | null;
    onCancel: () => void;
    onMerged: () => Promise<unknown>;
}

interface MergeValues {
    reason: string;
}

export const GovernanceMergeModal = ({ node, onCancel, onMerged }: GovernanceMergeModalProps) => {
    const { message } = App.useApp();
    const [form] = Form.useForm<MergeValues>();
    const [candidate, setCandidate] = useState<GraphGovernanceNodeRecord | null>(null);
    const [mergedNodes, setMergedNodes] = useState<GraphGovernanceNodeRecord[]>([]);
    const previewQuery = useQuery({
        enabled: Boolean(node && mergedNodes.length),
        queryFn: () =>
            service.previewPublishedNodeMerge({
                mergedNodeIds: mergedNodes.map((item) => item.id),
                retainedNodeId: node!.id
            }),
        queryKey: [
            "knowledge",
            "graph-governance",
            "merge-preview",
            node?.id,
            mergedNodes.map((item) => item.id)
        ]
    });
    const mergeMutation = useMutation({
        mutationFn: (values: MergeValues) => {
            if (!node || !previewQuery.data) throw new Error("合并影响尚未加载完成。");
            return service.mergePublishedNodes({
                impactToken: previewQuery.data.impactToken,
                mergedNodeIds: mergedNodes.map((item) => item.id),
                reason: values.reason,
                retainedNodeId: node.id,
                retainedNodeLockVersion: node.lockVersion ?? ""
            });
        },
        onError: (error) => message.error(error instanceof Error ? error.message : "合并节点失败"),
        onSuccess: async () => {
            message.success("节点已合并");
            await onMerged();
            onCancel();
        }
    });
    const addCandidate = () => {
        if (
            candidate &&
            candidate.id !== node?.id &&
            !mergedNodes.some((item) => item.id === candidate.id)
        ) {
            setMergedNodes((current) => [...current, candidate]);
            setCandidate(null);
        }
    };
    return (
        <KuzhambuModal
            confirmLoading={mergeMutation.isPending}
            okButtonProps={{ danger: true, disabled: !previewQuery.data?.executable }}
            okText="确认合并"
            open={Boolean(node)}
            testId="knowledge-graph-governance-merge-modal"
            title="合并节点"
            onCancel={onCancel}
            onOk={() => form.submit()}
        >
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <span>保留节点：{node?.name || node?.id}</span>
                <PublishedNodeRemoteSelect ariaLabel="搜索待合并节点" onSelectNode={setCandidate} />
                <KuzhambuButton
                    testId="knowledge-graph-governance-add-merge-node"
                    disabled={!candidate}
                    onClick={addCandidate}
                >
                    增加节点
                </KuzhambuButton>
                <KuzhambuSpace wrap>
                    {mergedNodes.map((item) => (
                        <KuzhambuTag
                            key={item.id}
                            closable
                            onClose={() =>
                                setMergedNodes((current) =>
                                    current.filter((value) => value.id !== item.id)
                                )
                            }
                        >
                            {item.name || item.id}
                        </KuzhambuTag>
                    ))}
                </KuzhambuSpace>
                {previewQuery.data ? (
                    <span>
                        将影响 {previewQuery.data.nodes.length} 个节点和{" "}
                        {previewQuery.data.edges.length} 条关系。
                    </span>
                ) : null}
                <KuzhambuForm form={form} onFinish={(values) => mergeMutation.mutate(values)}>
                    <KuzhambuFormItem
                        label="合并原因"
                        name="reason"
                        rules={[{ required: true, message: "请填写合并原因" }]}
                    >
                        <Input.TextArea maxLength={1024} rows={3} />
                    </KuzhambuFormItem>
                </KuzhambuForm>
            </KuzhambuSpace>
        </KuzhambuModal>
    );
};
