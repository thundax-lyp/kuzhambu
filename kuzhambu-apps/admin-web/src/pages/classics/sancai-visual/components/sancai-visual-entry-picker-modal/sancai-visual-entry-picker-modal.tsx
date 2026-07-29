import { Skeleton, Tree, Typography } from "antd";
import type { Key } from "react";
import { KuzhambuAlert, KuzhambuButton, KuzhambuModal } from "@/components";
import { useSancaiVisualEntryPickerState } from "@/pages/classics/sancai-visual/hooks/use-sancai-visual-entry-picker-state";
import type { SancaiEntryRecord } from "@/pages/classics/sancai-visual/sancai-visual-types";

const { Text } = Typography;

interface SancaiVisualEntryPickerModalProps {
    onCancel: () => void;
    onSelect: (entry: SancaiEntryRecord) => void;
    open: boolean;
}

export const SancaiVisualEntryPickerModal = ({
    onCancel,
    onSelect,
    open
}: SancaiVisualEntryPickerModalProps) => {
    const {
        expandedKeys,
        hasError,
        isCatalogLoading,
        pendingEntry,
        readEntryTitle,
        resetPendingEntry,
        selectedKeys,
        selectedVolume,
        selectNode,
        setExpandedKeys,
        treeData
    } = useSancaiVisualEntryPickerState(open);
    const confirmPendingEntry = () => {
        if (pendingEntry) {
            resetPendingEntry();
            onSelect(pendingEntry);
        }
    };
    const cancelPicker = () => {
        resetPendingEntry();
        onCancel();
    };

    return (
        <KuzhambuModal
            testId="classics-sancai-visual-entry-picker-modal"
            className="sancai-visual-entry-picker-modal"
            width={760}
            title="选择视觉处理稿件"
            footer={
                <div className="sancai-modal-footer">
                    <KuzhambuButton
                        testId="classics-sancai-visual-entry-picker-cancel-button"
                        onClick={cancelPicker}
                    >
                        取消
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="classics-sancai-visual-entry-picker-confirm-button"
                        type="primary"
                        disabled={!pendingEntry}
                        onClick={confirmPendingEntry}
                    >
                        选择
                    </KuzhambuButton>
                </div>
            }
            open={open}
            onCancel={cancelPicker}
        >
            {hasError ? (
                <KuzhambuAlert
                    className="sancai-alert"
                    type="warning"
                    showIcon
                    title="稿件选择数据加载失败"
                    description="请确认目录和稿件接口可用后重试。"
                />
            ) : null}
            <div className="sancai-visual-entry-picker" aria-label="三才图会视觉处理稿件选择">
                <div className="sancai-visual-entry-picker-tree">
                    {isCatalogLoading ? (
                        <Skeleton active paragraph={{ rows: 10 }} />
                    ) : (
                        <Tree
                            blockNode
                            showIcon
                            expandedKeys={expandedKeys}
                            selectedKeys={selectedKeys}
                            treeData={treeData}
                            onExpand={(keys: Key[]) => setExpandedKeys(keys.map(String))}
                            onSelect={selectNode}
                        />
                    )}
                </div>
                <div className="sancai-visual-entry-picker-hint">
                    {!selectedVolume && !isCatalogLoading ? (
                        <Text type="secondary">请先选择卷目，再选择稿件。</Text>
                    ) : pendingEntry ? (
                        <Text type="secondary">{`已选择：${readEntryTitle(pendingEntry)}`}</Text>
                    ) : (
                        <Text type="secondary">请选择一条稿件后确认。</Text>
                    )}
                </div>
            </div>
        </KuzhambuModal>
    );
};
