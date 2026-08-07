import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Splitter } from "antd";
import { useCallback, useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuAlert, KuzhambuButton, KuzhambuPage } from "@/components";
import { getCurrentUserInfo } from "@/service/current-user-service";
import type { OptionsRecord } from "@/types/options";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { UserBatchActions } from "./user-batch-actions";
import { ALL_DEPARTMENT_ID, UserDepartmentTree } from "./user-department-tree";
import { UserEditDrawer } from "./user-edit-drawer";
import { UserFilterPanel } from "./user-filter-panel";
import type { UserFilters, UserFilterStatus } from "./user-filter-panel";
import { UserPageActions } from "./user-page-actions";
import { UserTable } from "./user-table";
import * as service from "./user-service";
import type { PageQuery, UserOptionKeys } from "./user-service";
import type { UserDepartmentNode, UserRecord } from "./user-types";
import "./user-page.css";

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

const toEnableQueryValue = (enable: UserFilterStatus) => {
    if (enable === "ENABLED") {
        return true;
    }
    if (enable === "DISABLED") {
        return false;
    }
    return undefined;
};

const readErrorMessage = (error: unknown, fallback: string) => {
    return error instanceof Error ? error.message : fallback;
};

export const UserPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [query, setQuery] = useState<PageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [isFilterOpen, setIsFilterOpen] = useState(false);
    const [filters, setFilters] = useState<UserFilters>(DEFAULT_USER_FILTERS);
    const [selectedDepartmentId, setSelectedDepartmentId] = useState(ALL_DEPARTMENT_ID);
    const [editingUser, setEditingUser] = useState<UserRecord | null>(null);
    const [userEditDrawerOpen, setUserEditDrawerOpen] = useState(false);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const hasSelectedUsers = selectedRowKeys.length > 0;
    const hasActiveFilters = Boolean(filters.loginName.trim()) || filters.enable !== "ALL";
    const canEditUser = hasPermission("sys:user:edit");

    const userPageQuery = useQuery({
        queryKey: ["user", "page", query],
        queryFn: () => service.page(query),
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
    const departmentQuery = useQuery({
        queryKey: ["user", "department", "tree"],
        queryFn: service.listDepartments,
        retry: false
    });
    const canManageUser = canEditUser && currentUserQuery.isSuccess;
    const pageData = userPageQuery.data;
    const users = useMemo(() => pageData?.records ?? EMPTY_USERS, [pageData?.records]);
    const totalCount = pageData?.count ?? pageData?.totalCount ?? 0;
    const userOptions = userOptionsQuery.data ?? EMPTY_USER_OPTIONS;
    const departments = departmentQuery.data ?? EMPTY_DEPARTMENTS;
    const statusLabelByValue = useMemo(() => {
        return new Map(userOptions.statusOptions.map((option) => [option.value, option.label]));
    }, [userOptions.statusOptions]);
    const rankLabelByValue = useMemo(() => {
        return new Map(userOptions.rankOptions.map((option) => [option.value, option.label]));
    }, [userOptions.rankOptions]);
    const invalidatePage = async () => {
        await queryClient.invalidateQueries({ queryKey: ["user", "page"] });
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

    const updateQuery = useCallback((nextQuery: Partial<PageQuery>) => {
        setSelectedRowKeys([]);
        setQuery((currentQuery) => ({
            ...currentQuery,
            ...nextQuery,
            pageNo: nextQuery.pageNo || DEFAULT_PAGE_NO
        }));
    }, []);

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

    const selectDepartment = useCallback(
        (departmentId: string) => {
            setSelectedDepartmentId(departmentId);
            updateQuery({
                departmentId: departmentId === ALL_DEPARTMENT_ID ? undefined : departmentId,
                pageNo: DEFAULT_PAGE_NO
            });
        },
        [updateQuery]
    );

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
        setEditingUser(
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
        setUserEditDrawerOpen(true);
    };

    return (
        <>
            <KuzhambuPage
                className="user-page"
                title="用户管理"
                description="管理后台用户、角色与权限状态。"
                actions={
                    <UserPageActions
                        searchText={searchText}
                        filterOpen={isFilterOpen}
                        filterActive={hasActiveFilters}
                        isRefreshing={
                            userPageQuery.isFetching ||
                            userOptionsQuery.isFetching ||
                            currentUserQuery.isFetching ||
                            departmentQuery.isFetching
                        }
                        canCreateUser={canManageUser}
                        onSearch={searchUsers}
                        onToggleFilter={() => setIsFilterOpen((open) => !open)}
                        onRefresh={() => {
                            void userPageQuery.refetch();
                            void userOptionsQuery.refetch();
                            void currentUserQuery.refetch();
                            void departmentQuery.refetch();
                        }}
                        onCreate={openCreateUser}
                    />
                }
            >
                {currentUserQuery.isError ? (
                    <KuzhambuAlert
                        showIcon
                        type="error"
                        title="当前用户信息加载失败"
                        description="用户管理操作已暂停，请重试后继续。"
                        action={
                            <KuzhambuButton
                                ariaLabel="重试加载当前用户信息"
                                testId="system-user-current-user-retry-button"
                                onClick={() => void currentUserQuery.refetch()}
                            >
                                重试
                            </KuzhambuButton>
                        }
                    />
                ) : null}
                {userOptionsQuery.isError ? (
                    <KuzhambuAlert
                        showIcon
                        type="warning"
                        title="用户选项加载失败"
                        description="状态和等级名称可能显示为默认值。"
                        action={
                            <KuzhambuButton
                                ariaLabel="重试加载用户选项"
                                testId="system-user-options-retry-button"
                                onClick={() => void userOptionsQuery.refetch()}
                            >
                                重试
                            </KuzhambuButton>
                        }
                    />
                ) : null}
                <UserFilterPanel
                    open={isFilterOpen}
                    resetDisabled={!hasActiveFilters}
                    filters={filters}
                    statusOptions={userOptions.statusOptions}
                    loading={userOptionsQuery.isFetching}
                    onFiltersChange={setFilters}
                    onApply={() => {
                        applyFilters();
                        setIsFilterOpen(false);
                    }}
                    onReset={resetFilters}
                />
                <Splitter
                    className="user-department-work-area"
                    classNames={{
                        dragger: "user-department-work-area-dragger"
                    }}
                >
                    <Splitter.Panel
                        className="user-department-work-area-panel"
                        defaultSize={280}
                        min={220}
                        max={520}
                    >
                        <UserDepartmentTree
                            departments={departments}
                            error={
                                departmentQuery.isError
                                    ? new Error(
                                          readErrorMessage(departmentQuery.error, "部门加载失败")
                                      )
                                    : null
                            }
                            loading={departmentQuery.isFetching}
                            selectedDepartmentId={selectedDepartmentId}
                            onRetry={() => void departmentQuery.refetch()}
                            onSelectDepartment={selectDepartment}
                        />
                    </Splitter.Panel>
                    <Splitter.Panel className="user-work-panel">
                        {userPageQuery.isError ? (
                            <KuzhambuAlert
                                showIcon
                                type="error"
                                title="用户列表加载失败"
                                description={readErrorMessage(userPageQuery.error, "请稍后重试")}
                                action={
                                    <KuzhambuButton
                                        ariaLabel="重试加载用户列表"
                                        testId="system-user-page-retry-button"
                                        onClick={() => void userPageQuery.refetch()}
                                    >
                                        重试
                                    </KuzhambuButton>
                                }
                            />
                        ) : null}
                        {pageData || !userPageQuery.isError ? (
                            <UserTable
                                users={users}
                                loading={userPageQuery.isFetching}
                                currentPage={query.pageNo || DEFAULT_PAGE_NO}
                                pageSize={query.pageSize || DEFAULT_PAGE_SIZE}
                                totalCount={totalCount}
                                selectedRowKeys={selectedRowKeys}
                                batchActions={
                                    <UserBatchActions
                                        selectedCount={selectedRowKeys.length}
                                        canEditUser={canManageUser}
                                        statusPending={statusMutation.isPending}
                                        deletePending={deleteMutation.isPending}
                                        onDisable={() => batchUpdateStatus(false)}
                                        onEnable={() => batchUpdateStatus(true)}
                                        onDelete={batchDeleteUsers}
                                    />
                                }
                                statusPending={statusMutation.isPending}
                                canEditUser={canManageUser}
                                currentUser={currentUserQuery.data}
                                statusLabelByValue={statusLabelByValue}
                                rankLabelByValue={rankLabelByValue}
                                onSelectedRowKeysChange={setSelectedRowKeys}
                                onPageChange={(pageNo, pageSize) =>
                                    updateQuery({ pageNo, pageSize })
                                }
                                onStatusChange={updateSingleStatus}
                                onEdit={(user) => {
                                    setEditingUser(user);
                                    setUserEditDrawerOpen(true);
                                }}
                                onDelete={confirmDeleteUser}
                            />
                        ) : null}
                    </Splitter.Panel>
                </Splitter>
            </KuzhambuPage>

            <UserEditDrawer
                key={`${userEditDrawerOpen ? "open" : "closed"}-${editingUser?.id || "create"}-${currentUserQuery.data?.ranks ?? "rank"}`}
                open={userEditDrawerOpen}
                user={editingUser}
                currentUser={currentUserQuery.data}
                departments={departments}
                rankOptions={userOptions.rankOptions}
                onClose={() => {
                    setUserEditDrawerOpen(false);
                    setEditingUser(null);
                }}
            />
        </>
    );
};
