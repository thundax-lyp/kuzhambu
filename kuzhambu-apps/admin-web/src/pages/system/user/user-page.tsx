import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Splitter } from "antd";
import { useCallback, useMemo, useState } from "react";
import type { Key } from "react";
import { sm2 } from "sm-crypto";
import { createLoginForm } from "@/auth/auth-service";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { getCurrentUserInfo } from "@/service/current-user-service";
import type { OptionsRecord } from "@/types/options";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { UserBatchActions } from "./components/user-batch-actions";
import { ALL_DEPARTMENT_ID, UserDepartmentTree } from "./components/user-department-tree";
import { UserEditDrawer } from "./components/user-edit-drawer";
import { UserFilterPanel } from "./components/user-filter-panel";
import type { UserFilters, UserFilterStatus } from "./components/user-filter-panel";
import { UserPageActions } from "./components/user-page-actions";
import { UserTable } from "./components/user-table";
import * as service from "./user-service";
import type { PageQuery, SaveCommand, UserOptionKeys } from "./user-service";
import type { UserDepartmentNode, UserFormValues, UserRecord } from "./user-types";
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
    const [departments, setDepartments] = useState<UserDepartmentNode[]>(EMPTY_DEPARTMENTS);
    const [departmentTreeFetching, setDepartmentTreeFetching] = useState(false);
    const [departmentRefreshSignal, setDepartmentRefreshSignal] = useState(0);
    const [activeUser, setActiveUser] = useState<UserRecord | null>(null);
    const [userEditorOpen, setUserEditDrawerorOpen] = useState(false);
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
    const userOptions = userOptionsQuery.data ?? EMPTY_USER_OPTIONS;
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
            setUserEditDrawerorOpen(false);
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
            setUserEditDrawerorOpen(false);
            await invalidatePage();
            messageApi.success("用户已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "新增失败");
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

    const updateDepartments = useCallback((nextDepartments: UserDepartmentNode[]) => {
        setDepartments(nextDepartments);
    }, []);

    const updateDepartmentTreeFetching = useCallback((isFetching: boolean) => {
        setDepartmentTreeFetching(isFetching);
    }, []);

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
        setUserEditDrawerorOpen(true);
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
                        isRefreshing={userQuery.isFetching || departmentTreeFetching}
                        canCreateUser={canEditUser}
                        onSearch={searchUsers}
                        onToggleFilter={() => setIsFilterOpen((open) => !open)}
                        onRefresh={() => {
                            userQuery.refetch();
                            setDepartmentRefreshSignal((currentSignal) => currentSignal + 1);
                        }}
                        onCreate={openCreateUser}
                    />
                }
            >
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
                <UserBatchActions
                    selectedCount={selectedRowKeys.length}
                    canEditUser={canEditUser}
                    statusPending={statusMutation.isPending}
                    deletePending={deleteMutation.isPending}
                    onDisable={() => batchUpdateStatus(false)}
                    onEnable={() => batchUpdateStatus(true)}
                    onDelete={batchDeleteUsers}
                />
                <Splitter className="user-department-work-area">
                    <Splitter.Panel defaultSize={280} min={220} max={520}>
                        <UserDepartmentTree
                            refreshSignal={departmentRefreshSignal}
                            selectedDepartmentId={selectedDepartmentId}
                            onDepartmentsChange={updateDepartments}
                            onFetchingChange={updateDepartmentTreeFetching}
                            onSelectDepartment={selectDepartment}
                        />
                    </Splitter.Panel>
                    <Splitter.Panel className="user-work-panel">
                        <UserTable
                            users={users}
                            loading={userQuery.isFetching}
                            currentPage={query.pageNo || DEFAULT_PAGE_NO}
                            pageSize={query.pageSize || DEFAULT_PAGE_SIZE}
                            totalCount={totalCount}
                            selectedRowKeys={selectedRowKeys}
                            statusPending={statusMutation.isPending}
                            canEditUser={canEditUser}
                            currentUser={currentUserQuery.data}
                            statusLabelByValue={statusLabelByValue}
                            rankLabelByValue={rankLabelByValue}
                            onSelectedRowKeysChange={setSelectedRowKeys}
                            onPageChange={(pageNo, pageSize) => updateQuery({ pageNo, pageSize })}
                            onStatusChange={updateSingleStatus}
                            onEdit={(user) => {
                                setActiveUser(user);
                                setUserEditDrawerorOpen(true);
                            }}
                            onDelete={confirmDeleteUser}
                        />
                    </Splitter.Panel>
                </Splitter>
            </KuzhambuPage>

            <UserEditDrawer
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
                    setUserEditDrawerorOpen(false);
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
