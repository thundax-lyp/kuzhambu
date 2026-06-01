import {
    ApartmentOutlined,
    DeleteOutlined,
    PoweroffOutlined,
    ReloadOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Input, Select, Space, Tree, Typography } from "antd";
import type { DataNode } from "antd/es/tree";
import { useEffect, useMemo, useRef, useState } from "react";
import type { Key } from "react";
import { sm2 } from "sm-crypto";
import { createLoginForm } from "@/auth/auth-service";
import { hasPermission } from "@/auth/permission-storage";
import { SandwishListPage } from "@/components/sandwish-list-page";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import { SandwishTag } from "@/components/sandwish-tag";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { getCurrentUserInfo } from "@/service/current-user-service";
import type { CurrentUserRecord } from "@/service/current-user-types";
import type { OptionsRecord } from "@/types/options";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { UserAvatar } from "./components/user-avatar";
import { UserEdit } from "./components/user-edit";
import * as service from "./user-service";
import type { PageQuery, SaveCommand, UserOptionKeys } from "./user-service";
import type { UserDepartmentNode, UserFormValues, UserRecord } from "./user-types";
import "./user-page.css";

const { Text } = Typography;

const ALL_DEPARTMENT_ID = "all";
const DEPARTMENT_PANEL_BOTTOM_GAP = 8;

const DEFAULT_COLUMN_WIDTHS = {
    name: 230,
    loginName: 160,
    department: 190,
    roles: 190,
    status: 120,
    ranks: 90
};

type UserFilterStatus = "ALL" | "ENABLED" | "DISABLED";

interface UserFilters {
    loginName: string;
    enable: UserFilterStatus;
}

const DEFAULT_USER_FILTERS: UserFilters = {
    loginName: "",
    enable: "ALL"
};

const EMPTY_USERS: UserRecord[] = [];
const EMPTY_DEPARTMENTS: UserDepartmentNode[] = [];
const EMPTY_USER_OPTIONS: OptionsRecord<UserOptionKeys> = {
    statusOptions: [],
    rankOptions: []
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

const statusValue = (user: UserRecord): Exclude<UserFilterStatus, "ALL"> => {
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

const buildDepartmentTree = (departments: UserDepartmentNode[]): DataNode[] => {
    const rootDepartment: UserDepartmentNode = {
        id: ALL_DEPARTMENT_ID,
        parentId: null,
        name: "全部部门",
        shortName: "全部"
    };
    const allDepartments = [rootDepartment, ...departments];
    const childrenByParentId = new Map<string | null | undefined, UserDepartmentNode[]>();
    allDepartments.forEach((department) => {
        const parentId =
            department.parentId || (department.id === ALL_DEPARTMENT_ID ? null : ALL_DEPARTMENT_ID);
        const children = childrenByParentId.get(parentId) || [];
        children.push(department);
        childrenByParentId.set(parentId, children);
    });

    const toNode = (department: UserDepartmentNode): DataNode => ({
        key: department.id,
        title: (
            <span className="user-department-node">
                <span>{department.name}</span>
            </span>
        ),
        children: childrenByParentId.get(department.id)?.map(toNode)
    });

    return (childrenByParentId.get(null) || []).map(toNode);
};

const collectTreeKeys = (nodes: DataNode[]): Key[] => {
    return nodes.flatMap((node) => [
        node.key,
        ...(node.children ? collectTreeKeys(node.children) : [])
    ]);
};

const toEnableQueryValue = (enable: UserFilterStatus) => {
    if (enable === "ENABLED") {
        return true;
    }
    if (enable === "DISABLED") {
        return false;
    }
    return undefined;
};

export const UserPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const departmentPanelRef = useRef<HTMLDivElement | null>(null);
    const [query, setQuery] = useState<PageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<UserFilters>(DEFAULT_USER_FILTERS);
    const [selectedDepartmentId, setSelectedDepartmentId] = useState(ALL_DEPARTMENT_ID);
    const [activeUser, setActiveUser] = useState<UserRecord | null>(null);
    const [userEditorOpen, setUserEditorOpen] = useState(false);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const hasSelectedUsers = selectedRowKeys.length > 0;
    const hasActiveFilters = Boolean(filters.loginName.trim()) || filters.enable !== "ALL";
    const canEditUser = hasPermission("sys:user:edit");
    const isCreatingUser = userEditorOpen && !activeUser?.id;

    const userQuery = useQuery({
        queryKey: ["user", "page", query],
        queryFn: () => service.page(query),
        retry: false
    });
    const departmentQuery = useQuery({
        queryKey: ["user", "department", "tree"],
        queryFn: () => service.listDepartments(),
        retry: false
    });
    const userOptionsQuery = useQuery({
        queryKey: ["user", "options"],
        queryFn: service.getOptions,
        retry: false
    });
    const currentUserQuery = useQuery({
        queryKey: ["current-user", "info"],
        queryFn: getCurrentUserInfo,
        retry: false
    });
    const pageData = userQuery.data;
    const users = useMemo(() => pageData?.records ?? EMPTY_USERS, [pageData?.records]);
    const totalCount = pageData?.count ?? pageData?.totalCount ?? 0;
    const departments = useMemo(
        () => departmentQuery.data ?? EMPTY_DEPARTMENTS,
        [departmentQuery.data]
    );
    const userOptions = userOptionsQuery.data ?? EMPTY_USER_OPTIONS;
    const statusLabelByValue = useMemo(() => {
        return new Map(userOptions.statusOptions.map((option) => [option.value, option.label]));
    }, [userOptions.statusOptions]);
    const rankLabelByValue = useMemo(() => {
        return new Map(userOptions.rankOptions.map((option) => [option.value, option.label]));
    }, [userOptions.rankOptions]);
    const departmentTreeData = useMemo(() => buildDepartmentTree(departments), [departments]);
    const departmentTreeKeys = useMemo(
        () => collectTreeKeys(departmentTreeData),
        [departmentTreeData]
    );
    useEffect(() => {
        const departmentPanel = departmentPanelRef.current;
        if (!departmentPanel) {
            return undefined;
        }

        let frame = 0;
        const updateFloatingBounds = () => {
            frame = 0;
            const floatingContainer =
                departmentPanel.closest<HTMLElement>(".user-department-aside") ?? departmentPanel;
            const tableArea =
                departmentPanel.closest<HTMLElement>(".user-department-table-area") ??
                floatingContainer;
            const topbar = document.querySelector(".topbar")?.getBoundingClientRect();
            const sidebar = document.querySelector(".sidebar")?.getBoundingClientRect();
            const stickyTop = Math.ceil((topbar?.bottom ?? 76) + 12);
            const bottomInset = Math.max(
                12,
                Math.round(
                    window.innerHeight -
                        (sidebar?.bottom ?? window.innerHeight - 12) +
                        DEPARTMENT_PANEL_BOTTOM_GAP
                )
            );
            const floatingTop = Math.max(
                stickyTop,
                Math.ceil(floatingContainer.getBoundingClientRect().top)
            );

            floatingContainer.style.setProperty("--user-department-sticky-top", `${stickyTop}px`);
            floatingContainer.style.setProperty(
                "--user-department-floating-top",
                `${floatingTop}px`
            );
            floatingContainer.style.setProperty(
                "--user-department-floating-bottom",
                `${bottomInset}px`
            );
            tableArea.style.setProperty("--user-department-sticky-top", `${stickyTop}px`);
            tableArea.style.setProperty("--user-department-floating-bottom", `${bottomInset}px`);
        };
        const scheduleUpdate = () => {
            if (frame) {
                return;
            }
            frame = window.requestAnimationFrame(updateFloatingBounds);
        };

        updateFloatingBounds();
        window.addEventListener("scroll", scheduleUpdate, { passive: true });
        window.addEventListener("resize", scheduleUpdate);

        const observer =
            typeof ResizeObserver === "undefined" ? null : new ResizeObserver(scheduleUpdate);
        const topbarElement = document.querySelector(".topbar");
        const sidebarElement = document.querySelector(".sidebar");
        if (observer && topbarElement) {
            observer.observe(topbarElement);
        }
        if (observer && sidebarElement) {
            observer.observe(sidebarElement);
        }

        return () => {
            if (frame) {
                window.cancelAnimationFrame(frame);
            }
            window.removeEventListener("scroll", scheduleUpdate);
            window.removeEventListener("resize", scheduleUpdate);
            observer?.disconnect();
        };
    }, [departmentTreeData]);

    const invalidatePage = async () => {
        await queryClient.invalidateQueries({ queryKey: ["user", "page"] });
    };

    const readStatusLabel = (user: UserRecord) => {
        const value = statusValue(user);
        return statusLabelByValue.get(value) || (value === "DISABLED" ? "禁用" : "启用");
    };

    const readStatusOptionLabel = (value: Exclude<UserFilterStatus, "ALL">) => {
        return statusLabelByValue.get(value) || (value === "DISABLED" ? "禁用" : "启用");
    };

    const readRankLabel = (user: UserRecord) => {
        const value = user.superAdmin ? "9" : String(user.ranks ?? 0);
        return rankLabelByValue.get(value) || (user.superAdmin ? "超级管理员" : `等级 ${value}`);
    };

    const updateSingleStatus = (user: UserRecord, enable: boolean) => {
        statusMutation.mutate({
            users: [{ id: user.id, enable }]
        });
    };

    const statusMutation = useMutation({
        mutationFn: service.changeStatus,
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await invalidatePage();
            messageApi.success("用户状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "状态更新失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: service.remove,
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await invalidatePage();
            messageApi.success("用户已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });

    const avatarUploadMutation = useMutation({
        mutationFn: ({ id, avatar }: { id: string; avatar: File }) =>
            service.uploadAvatar(id, avatar),
        onSuccess: async (_, variables) => {
            const refreshedUsers = await userQuery.refetch();
            const refreshedUser = refreshedUsers.data?.records?.find(
                (user) => user.id === variables.id
            );
            if (refreshedUser) {
                setActiveUser(refreshedUser);
            }
            messageApi.success("头像已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "头像上传失败");
        }
    });
    const updateMutation = useMutation({
        mutationFn: service.changeInfo,
        onSuccess: async (savedUser) => {
            setActiveUser(savedUser);
            await invalidatePage();
            setUserEditorOpen(false);
            setActiveUser(null);
            messageApi.success("用户已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "更新失败");
        }
    });
    const createMutation = useMutation({
        mutationFn: async (form: UserFormValues) => {
            const loginForm = await createLoginForm();
            const encryptedPassword = sm2.doEncrypt(form.loginPass, loginForm.publicKey, 0);
            return service.create(
                toCreateSaveCommand(form, encryptedPassword, loginForm.loginToken)
            );
        },
        onSuccess: async () => {
            setUserEditorOpen(false);
            await invalidatePage();
            messageApi.success("用户已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "新增失败");
        }
    });

    const updateQuery = (nextQuery: Partial<PageQuery>) => {
        setSelectedRowKeys([]);
        setQuery((currentQuery) => ({
            ...currentQuery,
            ...nextQuery,
            pageNo: nextQuery.pageNo || DEFAULT_PAGE_NO
        }));
    };

    const searchUsers = (value: string) => {
        setSearchText(value);
        updateQuery({ name: normalizeSearch(value), pageNo: DEFAULT_PAGE_NO });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_USER_FILTERS);
        updateQuery({
            loginName: undefined,
            enable: undefined,
            pageNo: DEFAULT_PAGE_NO
        });
    };

    const applyFilters = () => {
        updateQuery({
            loginName: normalizeSearch(filters.loginName),
            enable: toEnableQueryValue(filters.enable),
            pageNo: DEFAULT_PAGE_NO
        });
    };

    const selectDepartment = (keys: Key[]) => {
        const nextDepartmentId = String(keys[0] || ALL_DEPARTMENT_ID);
        setSelectedDepartmentId(nextDepartmentId);
        updateQuery({
            departmentId: nextDepartmentId === ALL_DEPARTMENT_ID ? undefined : nextDepartmentId,
            pageNo: DEFAULT_PAGE_NO
        });
    };

    const confirmDeleteUser = (user: UserRecord) => {
        confirm.danger({
            title: "删除用户",
            message: `确认删除 ${readUserName(user)}？`,
            description: "删除后需要重新新增。若用户存在安全约束，接口会按后端校验结果拦截。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync([user.id])
        });
    };

    const batchDeleteUsers = () => {
        if (!hasSelectedUsers || !canEditUser) {
            return;
        }
        confirm.danger({
            title: "批量删除用户",
            message: `确认删除 ${selectedRowKeys.length} 个用户？`,
            description: "删除后需要重新新增。若用户存在安全约束，接口会按后端校验结果拦截。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(selectedRowKeys.map(String))
        });
    };

    const batchUpdateStatus = (enable: boolean) => {
        if (!hasSelectedUsers || !canEditUser) {
            return;
        }
        statusMutation.mutate({
            users: selectedRowKeys.map((id) => ({ id: String(id), enable }))
        });
    };

    const openCreateUser = () => {
        const selectedDepartment = departments.find(
            (department) => department.id === selectedDepartmentId
        );
        setActiveUser(
            selectedDepartment
                ? {
                      id: "",
                      name: "",
                      department: selectedDepartment,
                      roles: []
                  }
                : {
                      id: "",
                      name: "",
                      roles: []
                  }
        );
        setUserEditorOpen(true);
    };

    const toSaveCommand = (user: UserRecord, form: UserFormValues): SaveCommand => ({
        id: user.id,
        remarks: user.remarks,
        loginName: normalizeSearch(form.loginName),
        ranks: form.ranks,
        name: normalizeSearch(form.name),
        email: normalizeSearch(form.email),
        mobile: normalizeSearch(form.mobile),
        admin: form.admin,
        enable: form.enable,
        department: form.departmentId ? { id: form.departmentId } : null,
        roles: form.roleIds.map((roleId) => ({ id: roleId }))
    });

    const toCreateSaveCommand = (
        form: UserFormValues,
        encryptedPassword: string,
        token: string
    ): SaveCommand => ({
        loginName: normalizeSearch(form.loginName),
        loginPass: encryptedPassword,
        token,
        ranks: form.ranks,
        name: normalizeSearch(form.name),
        email: normalizeSearch(form.email),
        mobile: normalizeSearch(form.mobile),
        admin: form.admin,
        enable: form.enable,
        department: form.departmentId ? { id: form.departmentId } : null,
        roles: form.roleIds.map((roleId) => ({ id: roleId }))
    });

    const saveCreatingUser = (form: UserFormValues) => {
        if (!normalizeSearch(form.loginName)) {
            messageApi.error("请填写登录名");
            return;
        }
        if (!form.loginPass) {
            messageApi.error("请填写登录密码");
            return;
        }
        if (!normalizeSearch(form.name)) {
            messageApi.error("请填写姓名");
            return;
        }
        if (!form.departmentId) {
            messageApi.error("请选择部门");
            return;
        }
        createMutation.mutate(form);
    };

    const saveEditingUser = (form: UserFormValues) => {
        if (!activeUser) {
            return;
        }
        if (!normalizeSearch(form.loginName)) {
            messageApi.error("请填写登录名");
            return;
        }
        if (!normalizeSearch(form.name)) {
            messageApi.error("请填写姓名");
            return;
        }
        if (!form.departmentId) {
            messageApi.error("请选择部门");
            return;
        }
        updateMutation.mutate(toSaveCommand(activeUser, form));
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
                            <SandwishTag key={roleName} type={roleTagType(user, index)}>
                                {roleName}
                            </SandwishTag>
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
                const canManageCurrentUser = canManageUserByRank(currentUserQuery.data, user);
                return (
                    <KuzhambuSwitch
                        checked={user.enable !== false}
                        checkedChildren={readStatusOptionLabel("ENABLED")}
                        unCheckedChildren={readStatusOptionLabel("DISABLED")}
                        disabled={!canEditUser || !canManageCurrentUser || statusMutation.isPending}
                        aria-label={`切换 ${readUserName(user)} 状态，当前${readStatusLabel(user)}`}
                        onChange={(checked) => updateSingleStatus(user, checked)}
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
                <SandwishTag type={rankTagType(user)}>{readRankLabel(user)}</SandwishTag>
            )
        },
        {
            key: "actions",
            options: (user) => {
                const userName = readUserName(user);
                const canManageCurrentUser = canManageUserByRank(currentUserQuery.data, user);
                const disabled = !canEditUser || !canManageCurrentUser;
                return [
                    {
                        key: "edit",
                        text: "编辑",
                        ariaLabel: `编辑 ${userName}`,
                        disabled,
                        onClick: () => {
                            setActiveUser(user);
                            setUserEditorOpen(true);
                        }
                    },
                    { type: "divider" },
                    {
                        key: "delete",
                        text: "删除",
                        type: "danger",
                        ariaLabel: `删除 ${userName}`,
                        disabled,
                        onClick: () => confirmDeleteUser(user)
                    }
                ];
            }
        }
    ];

    return (
        <>
            <SandwishListPage<UserRecord>
                pageClassName="user-page"
                title="用户管理"
                description="管理后台用户、角色与权限状态。"
                subjectName="用户"
                enableAdd={canEditUser}
                addText="新增"
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                onSearchChange={searchUsers}
                onAdd={openCreateUser}
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "loginName",
                        label: "登录名",
                        render: () => (
                            <Input
                                allowClear
                                placeholder="developer"
                                value={filters.loginName}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        loginName: event.target.value
                                    }))
                                }
                            />
                        )
                    },
                    {
                        name: "enable",
                        label: "状态",
                        render: () => (
                            <Select<UserFilterStatus>
                                value={filters.enable}
                                options={[
                                    { value: "ALL", label: "全部" },
                                    ...userOptions.statusOptions.map((option) => ({
                                        value: option.value as UserFilterStatus,
                                        label: option.label
                                    }))
                                ]}
                                loading={userOptionsQuery.isFetching}
                                onChange={(enable) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        enable
                                    }))
                                }
                            />
                        )
                    }
                ]}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                pageActions={
                    <Button
                        icon={<ReloadOutlined />}
                        loading={userQuery.isFetching || departmentQuery.isFetching}
                        onClick={() => {
                            userQuery.refetch();
                            departmentQuery.refetch();
                        }}
                    >
                        刷新
                    </Button>
                }
                batchClassName="user-table-toolbar"
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <Space wrap>
                        <Button
                            className="user-batch-neutral"
                            icon={<PoweroffOutlined />}
                            disabled={!hasSelectedUsers || !canEditUser}
                            loading={statusMutation.isPending}
                            onClick={() => batchUpdateStatus(false)}
                        >
                            禁用
                        </Button>
                        <Button
                            className="user-batch-enable"
                            icon={<PoweroffOutlined />}
                            disabled={!hasSelectedUsers || !canEditUser}
                            loading={statusMutation.isPending}
                            onClick={() => batchUpdateStatus(true)}
                        >
                            启用
                        </Button>
                        <Button
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!hasSelectedUsers || !canEditUser}
                            loading={deleteMutation.isPending}
                            onClick={batchDeleteUsers}
                        >
                            批量删除
                        </Button>
                    </Space>
                }
                rowKey="id"
                className="user-table"
                columns={columns}
                dataSource={users}
                loading={userQuery.isFetching}
                pagination={{
                    current: query.pageNo || DEFAULT_PAGE_NO,
                    pageSize: query.pageSize || DEFAULT_PAGE_SIZE,
                    total: totalCount,
                    showTotal: (total) => `${total} 个用户`,
                    onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys,
                    getCheckboxProps: (user) => ({
                        disabled: !canEditUser || !canManageUserByRank(currentUserQuery.data, user)
                    })
                }}
                tableAsidePlacement="left"
                tableAreaClassName="user-department-table-area"
                tableAsideClassName="user-department-aside"
                tableAside={
                    <div className="user-department-panel" ref={departmentPanelRef}>
                        <div className="user-department-panel-head">
                            <Space size={8}>
                                <ApartmentOutlined />
                                <Text strong>部门</Text>
                            </Space>
                        </div>
                        <Tree
                            key={departmentTreeKeys.join(",")}
                            blockNode
                            defaultExpandedKeys={departmentTreeKeys}
                            selectedKeys={[selectedDepartmentId]}
                            treeData={departmentTreeData}
                            onSelect={selectDepartment}
                        />
                    </div>
                }
            />

            <UserEdit
                key={`${userEditorOpen ? "open" : "closed"}-${activeUser?.id || "create"}-${currentUserQuery.data?.ranks ?? "rank"}`}
                open={userEditorOpen}
                title={isCreatingUser ? "新增用户" : "编辑用户"}
                saveText="保存"
                user={activeUser}
                currentUser={currentUserQuery.data}
                departments={departments}
                rankOptions={userOptions.rankOptions}
                saving={isCreatingUser ? createMutation.isPending : updateMutation.isPending}
                onClose={() => {
                    setUserEditorOpen(false);
                    setActiveUser(null);
                }}
                onCreate={saveCreatingUser}
                onSave={saveEditingUser}
                onAvatarUpload={(avatar) => {
                    if (activeUser?.id) {
                        return avatarUploadMutation.mutateAsync({ id: activeUser.id, avatar });
                    }
                    return undefined;
                }}
            />
        </>
    );
};
