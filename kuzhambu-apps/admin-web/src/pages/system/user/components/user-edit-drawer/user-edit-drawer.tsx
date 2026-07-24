import { useQuery } from "@tanstack/react-query";
import { Input, Switch, TreeSelect } from "antd";
import type { TreeSelectProps } from "antd";
import { useMemo, useState } from "react";
import {
    KuzhambuButton,
    KuzhambuDrawer,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuFormPlaceholderItem,
    type UserDepartmentNode,
    type UserRecord,
    type UserRoleRecord
} from "@/pages/system/user/user-types";
import type { CurrentUserRecord } from "@/service/current-user-types";
import type { OptionsRecord } from "@/types/options";
import type { UserFormValues } from "../user-form-values";
import { UserAvatarField } from "./user-avatar-field";
import "./user-edit-drawer.css";
import { KuzhambuSelect } from "@/components";

import * as service from "@/pages/system/user/user-service";

interface UserEditDrawerProps {
    open?: boolean;
    title: string;
    saveText: string;
    user?: UserRecord | null;
    currentUser?: CurrentUserRecord | null;
    departments?: UserDepartmentNode[];
    rankOptions?: OptionsRecord[string];
    saving?: boolean;
    onClose: () => void;
    onSave?: (form: UserFormValues) => void;
    onCreate?: (form: UserFormValues) => void;
    onAvatarUpload?: (file: File) => Promise<unknown> | void;
}

const readRoleIds = (roles?: UserRoleRecord[] | null) => {
    return (roles || []).map((role) => role.id);
};

const readUserForm = (user: UserRecord): UserFormValues => ({
    loginName: user.loginName || "",
    loginPass: "",
    name: user.name || "",
    email: user.email || "",
    mobile: user.mobile || "",
    departmentId: user.department?.id || null,
    roleIds: readRoleIds(user.roles),
    ranks: user.ranks ?? 0,
    admin: Boolean(user.admin),
    enable: Boolean(user.enable)
});

const readRankValue = (user?: Pick<CurrentUserRecord, "ranks" | "superAdmin"> | null) => {
    if (!user) {
        return -1;
    }
    if (user.superAdmin) {
        return 9;
    }
    return user.ranks ?? 0;
};

const maxCreatableRank = (user?: Pick<CurrentUserRecord, "ranks" | "superAdmin"> | null) => {
    return Math.max(readRankValue(user) - 1, 0);
};

const fallbackRankOptions = (maxRank: number) => {
    return Array.from({ length: Math.max(maxRank, 0) + 1 }, (_, rank) => ({
        value: rank,
        label: `等级 ${rank}`
    }));
};

const toEditableRankOptions = (options: OptionsRecord[string] | undefined, maxRank: number) => {
    const sourceOptions = options?.length ? options : fallbackRankOptions(maxRank);
    return sourceOptions
        .map((option) => ({
            value: Number(option.value),
            label: option.label
        }))
        .filter((option) => Number.isFinite(option.value) && option.value <= maxRank);
};

type DepartmentTreeOption = NonNullable<TreeSelectProps["treeData"]>[number];

const departmentTreeOptions = (departments: UserDepartmentNode[]): DepartmentTreeOption[] => {
    const departmentById = new Map(departments.map((department) => [department.id, department]));
    const childrenByParentId = new Map<string | null | undefined, UserDepartmentNode[]>();
    departments.forEach((department) => {
        const parentId = departmentById.has(department.parentId || "") ? department.parentId : null;
        const children = childrenByParentId.get(parentId) || [];
        children.push(department);
        childrenByParentId.set(parentId, children);
    });

    const toNode = (department: UserDepartmentNode): DepartmentTreeOption => ({
        value: department.id,
        title: department.name,
        children: childrenByParentId.get(department.id)?.map(toNode)
    });

    return (childrenByParentId.get(null) || []).map(toNode);
};

const EMPTY_USER_ROLES: UserRoleRecord[] = [];

const DEFAULT_CREATE_USER_FORM: UserFormValues = {
    loginName: "",
    loginPass: "",
    name: "",
    email: "",
    mobile: "",
    departmentId: null,
    roleIds: [],
    ranks: 0,
    admin: false,
    enable: true
};

const readInitialFormValues = (
    user: UserRecord | null | undefined,
    editing: boolean,
    editableMaxRank: number
): UserFormValues => {
    const initialForm =
        user && editing
            ? readUserForm(user)
            : {
                  ...DEFAULT_CREATE_USER_FORM,
                  departmentId: user?.department?.id || null,
                  ranks: editableMaxRank
              };
    return initialForm.ranks > editableMaxRank
        ? { ...initialForm, ranks: editableMaxRank }
        : initialForm;
};

const readFormResetKey = (
    user: UserRecord | null | undefined,
    editing: boolean,
    visible: boolean,
    editableMaxRank: number
) => {
    if (!visible) {
        return "closed";
    }
    if (editing) {
        return `edit:${user?.id || ""}:${editableMaxRank}`;
    }
    return `create:${user?.department?.id || ""}:${editableMaxRank}`;
};

export const UserEditDrawer = ({
    open,
    title,
    saveText,
    user,
    currentUser,
    departments = [],
    rankOptions = [],
    saving,
    onClose,
    onSave,
    onCreate,
    onAvatarUpload
}: UserEditDrawerProps) => {
    const editing = Boolean(user?.id);
    const visible = Boolean(open);
    const creating = visible && !editing;
    const editableMaxRank = currentUser ? maxCreatableRank(currentUser) : (user?.ranks ?? 0);
    const editableRankOptions = useMemo(
        () => toEditableRankOptions(rankOptions, editableMaxRank),
        [editableMaxRank, rankOptions]
    );
    const formResetKey = readFormResetKey(user, editing, visible, editableMaxRank);
    const [formState, setFormState] = useState(() => ({
        resetKey: formResetKey,
        values: readInitialFormValues(user, editing, editableMaxRank)
    }));
    const shouldResetForm = visible && formState.resetKey !== formResetKey;
    const formValues = shouldResetForm
        ? readInitialFormValues(user, editing, editableMaxRank)
        : formState.values;
    if (shouldResetForm) {
        setFormState({
            resetKey: formResetKey,
            values: formValues
        });
    }
    const updateForm = (values: Partial<UserFormValues>) => {
        setFormState((currentFormState) => ({
            resetKey: formResetKey,
            values: {
                ...(currentFormState.resetKey === formResetKey
                    ? currentFormState.values
                    : formValues),
                ...values
            }
        }));
    };
    const userRoleQuery = useQuery({
        queryKey: ["user", "role", "list"],
        queryFn: () => service.listRoles(),
        enabled: visible,
        retry: false
    });
    const roleById = useMemo(() => {
        const roleById = new Map<string, UserRoleRecord>();
        [...(userRoleQuery.data ?? EMPTY_USER_ROLES), ...(user?.roles ?? [])].forEach((role) => {
            if (role?.id) {
                roleById.set(role.id, role);
            }
        });
        return roleById;
    }, [user?.roles, userRoleQuery.data]);
    const roleOptions = useMemo(() => {
        return Array.from(roleById.values()).map((role) => ({
            value: role.id,
            label: role.name || role.id
        }));
    }, [roleById]);
    const departmentOptions = useMemo(() => departmentTreeOptions(departments), [departments]);
    const saveForm = () => {
        if (creating) {
            onCreate?.(formValues);
            return;
        }
        onSave?.(formValues);
    };

    return (
        <KuzhambuDrawer
            testId="system-user-user-edit-drawer"
            className="user-edit-drawer"
            title={title}
            open={visible}
            size="large"
            onClose={onClose}
            extra={
                creating ? null : (
                    <KuzhambuButton testId="system-user-user-remove-button" size="small">
                        −
                    </KuzhambuButton>
                )
            }
            footerActions={[
                {
                    testId: "system-user-user-cancel-button",
                    title: "取消",
                    disabled: saving,
                    action: onClose
                },
                {
                    testId: "system-user-user-action-button",
                    title: saveText,
                    type: "primary",
                    loading: saving,
                    action: saveForm
                }
            ]}
        >
            <KuzhambuForm className="user-edit-drawer-form" component="div">
                {!creating && user ? (
                    <KuzhambuFormItem label="头像" layoutSize="large">
                        <UserAvatarField user={user} onAvatarUpload={onAvatarUpload} />
                    </KuzhambuFormItem>
                ) : null}
                <KuzhambuFormItem label="登录名" layoutSize="middle">
                    <Input
                        value={formValues.loginName}
                        placeholder="lin.zhiyuan"
                        onChange={(event) => updateForm({ loginName: event.target.value })}
                    />
                </KuzhambuFormItem>
                {creating ? (
                    <KuzhambuFormItem label="登录密码" layoutSize="middle">
                        <Input.Password
                            value={formValues.loginPass}
                            placeholder="设置初始密码"
                            onChange={(event) => updateForm({ loginPass: event.target.value })}
                        />
                    </KuzhambuFormItem>
                ) : null}
                <KuzhambuFormPlaceholderItem layoutSize="large" />
                <KuzhambuFormItem label="姓名" layoutSize="middle">
                    <Input
                        value={formValues.name}
                        placeholder="用户姓名"
                        onChange={(event) => updateForm({ name: event.target.value })}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem layoutSize="large" />
                <KuzhambuFormItem label="邮箱">
                    <Input
                        value={formValues.email || ""}
                        placeholder="name@example.com"
                        onChange={(event) => updateForm({ email: event.target.value })}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="手机">
                    <Input
                        value={formValues.mobile || ""}
                        placeholder="手机号"
                        onChange={(event) => updateForm({ mobile: event.target.value })}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="部门" layoutSize="middle">
                    <TreeSelect
                        value={formValues.departmentId || undefined}
                        treeData={departmentOptions}
                        placeholder="选择部门"
                        showSearch
                        treeDefaultExpandAll
                        treeNodeFilterProp="title"
                        onChange={(departmentId) => updateForm({ departmentId })}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem layoutSize="large" />
                <KuzhambuFormItem label="角色" layoutSize="middle">
                    <KuzhambuSelect
                        mode="multiple"
                        value={formValues.roleIds}
                        options={roleOptions}
                        loading={userRoleQuery.isFetching}
                        placeholder="选择角色"
                        onChange={(roleIds) => updateForm({ roleIds })}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="等级" layoutSize="small">
                    <KuzhambuSelect
                        value={formValues.ranks}
                        options={editableRankOptions}
                        onChange={(ranks) => updateForm({ ranks })}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem layoutSize="large" />
                <KuzhambuFormItem label="管理员" layoutSize="small">
                    <Switch
                        checked={formValues.admin}
                        onChange={(admin) => updateForm({ admin })}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem layoutSize="large" />
                <KuzhambuFormItem label="启用" layoutSize="small">
                    <Switch
                        checked={formValues.enable}
                        onChange={(enable) => updateForm({ enable })}
                    />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuDrawer>
    );
};
