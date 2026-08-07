import { useSyncExternalStore } from "react";
import { getPermissions, subscribePermissionsChange } from "@/auth/permission-storage";

const getPermissionSnapshot = () => getPermissions().join("\n");

export const usePermission = (permission: string) => {
    const permissionSnapshot = useSyncExternalStore(
        subscribePermissionsChange,
        getPermissionSnapshot,
        getPermissionSnapshot
    );
    return permissionSnapshot.split("\n").includes(permission);
};
