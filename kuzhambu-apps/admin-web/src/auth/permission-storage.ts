const PERMISSIONS_KEY = "kuzhambu.admin.permissions";
const PERMISSIONS_CHANGE_EVENT = "kuzhambu.admin.permissions.change";

const readStoredPermissions = () => {
    const permissions = localStorage.getItem(PERMISSIONS_KEY);
    if (!permissions) {
        return [];
    }

    try {
        const parsed = JSON.parse(permissions);
        return Array.isArray(parsed)
            ? parsed.filter((permission) => typeof permission === "string")
            : [];
    } catch {
        return [];
    }
};

let permissionSet = new Set<string>(readStoredPermissions());

const notifyPermissionsChange = () => {
    window.dispatchEvent(new Event(PERMISSIONS_CHANGE_EVENT));
};

export const replacePermissions = (permissions: string[]) => {
    permissionSet = new Set(permissions);
    localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(Array.from(permissionSet)));
    notifyPermissionsChange();
};

export const clearPermissions = () => {
    permissionSet = new Set();
    localStorage.removeItem(PERMISSIONS_KEY);
    notifyPermissionsChange();
};

export const getPermissions = () => {
    return Array.from(permissionSet);
};

export const hasPermission = (permission: string) => {
    return permissionSet.has(permission);
};

export const subscribePermissionsChange = (listener: () => void) => {
    const handleStorageChange = (event: StorageEvent) => {
        if (event.key !== PERMISSIONS_KEY && event.key !== null) {
            return;
        }
        permissionSet = new Set(readStoredPermissions());
        listener();
    };

    window.addEventListener(PERMISSIONS_CHANGE_EVENT, listener);
    window.addEventListener("storage", handleStorageChange);

    return () => {
        window.removeEventListener(PERMISSIONS_CHANGE_EVENT, listener);
        window.removeEventListener("storage", handleStorageChange);
    };
};
