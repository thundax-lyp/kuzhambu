import { DeleteOutlined, PoweroffOutlined } from "@ant-design/icons";
import { KuzhambuSpace, KuzhambuButton } from "@/components";

interface UserBatchActionsProps {
    canEditUser: boolean;
    deletePending: boolean;
    selectedCount: number;
    statusPending: boolean;
    onDelete: () => void;
    onDisable: () => void;
    onEnable: () => void;
}

export const UserBatchActions = ({
    canEditUser,
    deletePending,
    selectedCount,
    statusPending,
    onDelete,
    onDisable,
    onEnable
}: UserBatchActionsProps) => {
    const hasSelectedUsers = selectedCount > 0;

    return (
        <KuzhambuSpace wrap>
            <KuzhambuButton
                testId="system-user-user-batch-actions-disable-button"
                className="user-batch-neutral"
                icon={<PoweroffOutlined />}
                disabled={!hasSelectedUsers || !canEditUser}
                loading={statusPending}
                onClick={onDisable}
            >
                禁用
            </KuzhambuButton>
            <KuzhambuButton
                testId="system-user-user-batch-actions-enable-button"
                className="user-batch-enable"
                icon={<PoweroffOutlined />}
                disabled={!hasSelectedUsers || !canEditUser}
                loading={statusPending}
                onClick={onEnable}
            >
                启用
            </KuzhambuButton>
            <KuzhambuButton
                testId="system-user-user-batch-actions-batch-delete-button"
                danger
                icon={<DeleteOutlined />}
                disabled={!hasSelectedUsers || !canEditUser}
                loading={deletePending}
                onClick={onDelete}
            >
                批量删除
            </KuzhambuButton>
        </KuzhambuSpace>
    );
};
