import { DeleteOutlined, PoweroffOutlined } from "@ant-design/icons";
import { KuzhambuBatchActionBar } from "@/components/kuzhambu-batch-action-bar";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuButton } from "@/components/kuzhambu-button";

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
        <KuzhambuBatchActionBar
            className="user-table-toolbar"
            selectedCount={selectedCount}
            actions={
                <KuzhambuSpace wrap>
                    <KuzhambuButton
                        name="禁用"
                        className="user-batch-neutral"
                        icon={<PoweroffOutlined />}
                        disabled={!hasSelectedUsers || !canEditUser}
                        loading={statusPending}
                        onClick={onDisable}
                    >
                        禁用
                    </KuzhambuButton>
                    <KuzhambuButton
                        name="启用"
                        className="user-batch-enable"
                        icon={<PoweroffOutlined />}
                        disabled={!hasSelectedUsers || !canEditUser}
                        loading={statusPending}
                        onClick={onEnable}
                    >
                        启用
                    </KuzhambuButton>
                    <KuzhambuButton
                        name="批量删除"
                        danger
                        icon={<DeleteOutlined />}
                        disabled={!hasSelectedUsers || !canEditUser}
                        loading={deletePending}
                        onClick={onDelete}
                    >
                        批量删除
                    </KuzhambuButton>
                </KuzhambuSpace>
            }
        />
    );
};
