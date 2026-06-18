import { Space, Typography } from "antd";
import type { Key } from "react";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import type { CurrentUserRecord } from "@/service/current-user-types";
import type { UserRecord } from "../user-types";
import { UserAvatar } from "./user-avatar";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 230,
    loginName: 160,
    department: 190,
    roles: 190,
    status: 120,
    ranks: 90
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readUserName = (user: UserRecord) => {
    return normalizeSearch(user.name) || normalizeSearch(user.loginName) || `用户 ${user.id}`;
};

const readDepartmentName = (user: UserRecord) => {
    return user.department?.namePath || user.department?.name || "";
};

const readRoleNames = (user: UserRecord) => {
    return (user.roles || []).map((role) => role.name).filter(Boolean);
};

const statusValue = (user: UserRecord): "DISABLED" | "ENABLED" => {
    return user.enable === false ? "DISABLED" : "ENABLED";
};

const rankTagType = (user: UserRecord) => {
    return user.superAdmin || user.ranks === 9 ? "accent" : "info";
};

const roleTagType = (user: UserRecord, index: number) => {
    if (user.admin || user.superAdmin) {
        return "accent";
    }
    return index === 0 ? "info" : "neutral";
};

const readRankValue = (user?: Pick<UserRecord, "ranks" | "superAdmin"> | null) => {
    if (!user) {
        return -1;
    }
    if (user.superAdmin) {
        return 9;
    }
    return user.ranks ?? 0;
};

const canManageUserByRank = (
    currentUser: CurrentUserRecord | undefined,
    targetUser: UserRecord
) => {
    if (!currentUser || currentUser.id === targetUser.id) {
        return false;
    }
    return readRankValue(targetUser) < readRankValue(currentUser);
};

interface UserTableProps {
    canEditUser: boolean;
    currentPage: number;
    currentUser?: CurrentUserRecord;
    loading: boolean;
    pageSize: number;
    rankLabelByValue: Map<string, string>;
    selectedRowKeys: Key[];
    statusLabelByValue: Map<string, string>;
    statusPending: boolean;
    totalCount: number;
    users: UserRecord[];
    onDelete: (user: UserRecord) => void;
    onEdit: (user: UserRecord) => void;
    onPageChange: (pageNo: number, pageSize: number) => void;
    onSelectedRowKeysChange: (keys: Key[]) => void;
    onStatusChange: (user: UserRecord, enable: boolean) => void;
}

export const UserTable = ({
    canEditUser,
    currentPage,
    currentUser,
    loading,
    pageSize,
    rankLabelByValue,
    selectedRowKeys,
    statusLabelByValue,
    statusPending,
    totalCount,
    users,
    onDelete,
    onEdit,
    onPageChange,
    onSelectedRowKeysChange,
    onStatusChange
}: UserTableProps) => {
    const readStatusLabel = (user: UserRecord) => {
        const value = statusValue(user);
        return statusLabelByValue.get(value) || (value === "DISABLED" ? "禁用" : "启用");
    };

    const readStatusOptionLabel = (value: "DISABLED" | "ENABLED") => {
        return statusLabelByValue.get(value) || (value === "DISABLED" ? "禁用" : "启用");
    };

    const readRankLabel = (user: UserRecord) => {
        const value = user.superAdmin ? "9" : String(user.ranks ?? 0);
        return rankLabelByValue.get(value) || (user.superAdmin ? "超级管理员" : `等级 ${value}`);
    };

    const columns: KuzhambuTableProps<UserRecord>["columns"] = [
        {
            title: "用户",
            dataIndex: "name",
            key: "name",
            width: DEFAULT_COLUMN_WIDTHS.name,
            render: (_, user) => {
                const userName = readUserName(user);
                return (
                    <Space size={10}>
                        <UserAvatar user={user} />
                        <div className="user-name-cell">
                            <Text strong>{userName}</Text>
                            {user.email ? <Text type="secondary">{user.email}</Text> : null}
                        </div>
                    </Space>
                );
            }
        },
        {
            title: "登录名",
            dataIndex: "loginName",
            key: "loginName",
            width: DEFAULT_COLUMN_WIDTHS.loginName,
            render: (loginName?: string | null) => loginName || null
        },
        {
            title: "部门",
            key: "department",
            width: DEFAULT_COLUMN_WIDTHS.department,
            render: (_, user) => readDepartmentName(user) || null
        },
        {
            title: "角色",
            key: "roles",
            width: DEFAULT_COLUMN_WIDTHS.roles,
            render: (_, user) => {
                const roleNames = readRoleNames(user);
                if (!roleNames.length) {
                    return null;
                }
                return (
                    <Space size={[4, 4]} wrap>
                        {roleNames.map((roleName, index) => (
                            <KuzhambuTag key={roleName} type={roleTagType(user, index)}>
                                {roleName}
                            </KuzhambuTag>
                        ))}
                    </Space>
                );
            }
        },
        {
            title: "状态",
            dataIndex: "enable",
            key: "status",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (_, user) => {
                const canManageCurrentUser = canManageUserByRank(currentUser, user);
                return (
                    <KuzhambuSwitch
                        checked={user.enable !== false}
                        checkedChildren={readStatusOptionLabel("ENABLED")}
                        unCheckedChildren={readStatusOptionLabel("DISABLED")}
                        disabled={!canEditUser || !canManageCurrentUser || statusPending}
                        aria-label={`切换 ${readUserName(user)} 状态，当前${readStatusLabel(user)}`}
                        onChange={(checked) => onStatusChange(user, checked)}
                    />
                );
            }
        },
        {
            title: "级别",
            dataIndex: "ranks",
            key: "ranks",
            width: DEFAULT_COLUMN_WIDTHS.ranks,
            render: (_, user) => (
                <KuzhambuTag type={rankTagType(user)}>{readRankLabel(user)}</KuzhambuTag>
            )
        },
        {
            key: "actions",
            options: (user) => {
                const userName = readUserName(user);
                const canManageCurrentUser = canManageUserByRank(currentUser, user);
                const disabled = !canEditUser || !canManageCurrentUser;
                return [
                    {
                        key: "edit",
                        text: "编辑",
                        ariaLabel: `编辑 ${userName}`,
                        disabled,
                        onClick: () => onEdit(user)
                    },
                    { type: "divider" },
                    {
                        key: "delete",
                        text: "删除",
                        type: "danger",
                        ariaLabel: `删除 ${userName}`,
                        disabled,
                        onClick: () => onDelete(user)
                    }
                ];
            }
        }
    ];

    return (
        <KuzhambuTable<UserRecord>
            ariaLabel="用户列表"
            rowKey="id"
            className="user-table"
            columns={columns}
            dataSource={users}
            loading={loading}
            pagination={{
                current: currentPage,
                pageSize,
                total: totalCount,
                showTotal: (total) => `${total} 个用户`,
                onChange: onPageChange
            }}
            rowSelection={{
                selectedRowKeys,
                onChange: onSelectedRowKeysChange,
                getCheckboxProps: (user) => ({
                    disabled: !canEditUser || !canManageUserByRank(currentUser, user)
                })
            }}
        />
    );
};
