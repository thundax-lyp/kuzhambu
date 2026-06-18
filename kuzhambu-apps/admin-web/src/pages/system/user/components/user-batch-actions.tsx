import { DeleteOutlined, PoweroffOutlined } from "@ant-design/icons";
import { Button, Space } from "antd";
import { KuzhambuBatchActionBar } from "@/components/kuzhambu-batch-action-bar";

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
                <Space wrap>
                    <Button
                        className="user-batch-neutral"
                        icon={<PoweroffOutlined />}
                        disabled={!hasSelectedUsers || !canEditUser}
                        loading={statusPending}
                        onClick={onDisable}
                    >
                        禁用
                    </Button>
                    <Button
                        className="user-batch-enable"
                        icon={<PoweroffOutlined />}
                        disabled={!hasSelectedUsers || !canEditUser}
                        loading={statusPending}
                        onClick={onEnable}
                    >
                        启用
                    </Button>
                    <Button
                        danger
                        icon={<DeleteOutlined />}
                        disabled={!hasSelectedUsers || !canEditUser}
                        loading={deletePending}
                        onClick={onDelete}
                    >
                        批量删除
                    </Button>
                </Space>
            }
        />
    );
};
