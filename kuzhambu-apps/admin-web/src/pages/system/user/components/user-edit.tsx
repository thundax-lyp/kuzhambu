import { CameraOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Button, Input, Select, Switch, Upload } from "antd";
import { useMemo, useState } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { CurrentUserRecord } from "@/service/current-user-types";
import type { OptionsRecord } from "@/types/options";
import * as service from "../user-service";
import type { UserDepartmentNode, UserFormValues, UserRecord, UserRoleRecord } from "../user-types";
import { UserAvatar } from "./user-avatar";

interface UserEditProps {
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

const departmentOptions = (departments: UserDepartmentNode[]) => {
    return departments.map((department) => ({
        value: department.id,
        label: department.namePath || department.name
    }));
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

export const UserEdit = ({
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
}: UserEditProps) => {
    const [avatarUploading, setAvatarUploading] = useState(false);
    const editing = Boolean(user?.id);
    const visible = Boolean(open);
    const creating = visible && !editing;
    const editableMaxRank = currentUser ? maxCreatableRank(currentUser) : (user?.ranks ?? 0);
    const editableRankOptions = useMemo(
        () => toEditableRankOptions(rankOptions, editableMaxRank),
        [editableMaxRank, rankOptions]
    );
    const [createForm, setCreateForm] = useState<UserFormValues>(() => {
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
    });
    const updateForm = (values: Partial<UserFormValues>) => {
        setCreateForm((currentForm) => ({ ...currentForm, ...values }));
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
    const saveForm = () => {
        if (creating) {
            onCreate?.(createForm);
            return;
        }
        onSave?.(createForm);
    };

    return (
        <KuzhambuDrawer
            className="user-edit-drawer"
            title={title}
            open={visible}
            size="small"
            onClose={onClose}
            extra={creating ? null : <Button size="small">−</Button>}
            footer={
                <div className="user-edit-footer">
                    <Button disabled={saving} onClick={onClose}>
                        取消
                    </Button>
                    <Button type="primary" loading={saving} onClick={saveForm}>
                        {saveText}
                    </Button>
                </div>
            }
        >
            {!creating && user ? (
                <div className="user-edit-form">
                    <div className="user-edit-avatar">
                        <UserAvatar user={user} size={64} />
                        <Upload
                            accept="image/*"
                            showUploadList={false}
                            beforeUpload={(file) => {
                                const uploadResult = onAvatarUpload?.(file);
                                if (uploadResult) {
                                    setAvatarUploading(true);
                                    Promise.resolve(uploadResult)
                                        .finally(() => setAvatarUploading(false))
                                        .catch(() => undefined);
                                }
                                return Upload.LIST_IGNORE;
                            }}
                        >
                            <Button
                                size="small"
                                shape="circle"
                                icon={<CameraOutlined />}
                                loading={avatarUploading}
                            />
                        </Upload>
                    </div>
                    <label>
                        <span>姓名</span>
                        <Input
                            value={createForm.name}
                            placeholder="用户姓名"
                            onChange={(event) => updateForm({ name: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>登录名</span>
                        <Input
                            value={createForm.loginName}
                            placeholder="lin.zhiyuan"
                            onChange={(event) => updateForm({ loginName: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>邮箱</span>
                        <Input
                            value={createForm.email || ""}
                            placeholder="name@example.com"
                            onChange={(event) => updateForm({ email: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>手机</span>
                        <Input
                            value={createForm.mobile || ""}
                            placeholder="手机号"
                            onChange={(event) => updateForm({ mobile: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>部门</span>
                        <Select
                            value={createForm.departmentId || undefined}
                            options={departmentOptions(departments)}
                            placeholder="选择部门"
                            showSearch
                            optionFilterProp="label"
                            onChange={(departmentId) => updateForm({ departmentId })}
                        />
                    </label>
                    <label>
                        <span>角色</span>
                        <Select
                            mode="multiple"
                            value={createForm.roleIds}
                            options={roleOptions}
                            loading={userRoleQuery.isFetching}
                            placeholder="选择角色"
                            onChange={(roleIds) => updateForm({ roleIds })}
                        />
                    </label>
                    <label>
                        <span>等级</span>
                        <Select
                            value={createForm.ranks}
                            options={editableRankOptions}
                            onChange={(ranks) => updateForm({ ranks })}
                        />
                    </label>
                    <label className="user-edit-switch-row">
                        <span>管理员</span>
                        <Switch
                            checked={createForm.admin}
                            onChange={(admin) => updateForm({ admin })}
                        />
                    </label>
                    <label className="user-edit-switch-row">
                        <span>启用</span>
                        <Switch
                            checked={createForm.enable}
                            onChange={(enable) => updateForm({ enable })}
                        />
                    </label>
                </div>
            ) : null}
            {creating ? (
                <div className="user-edit-form">
                    <label>
                        <span>登录名</span>
                        <Input
                            value={createForm.loginName}
                            placeholder="lin.zhiyuan"
                            onChange={(event) => updateForm({ loginName: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>登录密码</span>
                        <Input.Password
                            value={createForm.loginPass}
                            placeholder="设置初始密码"
                            onChange={(event) => updateForm({ loginPass: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>姓名</span>
                        <Input
                            value={createForm.name}
                            placeholder="用户姓名"
                            onChange={(event) => updateForm({ name: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>邮箱</span>
                        <Input
                            value={createForm.email || ""}
                            placeholder="name@example.com"
                            onChange={(event) => updateForm({ email: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>手机</span>
                        <Input
                            value={createForm.mobile || ""}
                            placeholder="手机号"
                            onChange={(event) => updateForm({ mobile: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>部门</span>
                        <Select
                            value={createForm.departmentId || undefined}
                            options={departmentOptions(departments)}
                            placeholder="选择部门"
                            showSearch
                            optionFilterProp="label"
                            onChange={(departmentId) => updateForm({ departmentId })}
                        />
                    </label>
                    <label>
                        <span>角色</span>
                        <Select
                            mode="multiple"
                            value={createForm.roleIds}
                            options={roleOptions}
                            loading={userRoleQuery.isFetching}
                            placeholder="选择角色"
                            onChange={(roleIds) => updateForm({ roleIds })}
                        />
                    </label>
                    <label>
                        <span>等级</span>
                        <Select
                            value={createForm.ranks}
                            options={editableRankOptions}
                            onChange={(ranks) => updateForm({ ranks })}
                        />
                    </label>
                    <label className="user-edit-switch-row">
                        <span>管理员</span>
                        <Switch
                            checked={createForm.admin}
                            onChange={(admin) => updateForm({ admin })}
                        />
                    </label>
                    <label className="user-edit-switch-row">
                        <span>启用</span>
                        <Switch
                            checked={createForm.enable}
                            onChange={(enable) => updateForm({ enable })}
                        />
                    </label>
                </div>
            ) : null}
        </KuzhambuDrawer>
    );
};
