const PERMISSIONS_KEY = "kuzhambu.admin.permissions";

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

export const replacePermissions = (permissions: string[]) => {
    permissionSet = new Set(permissions);
    localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(Array.from(permissionSet)));
};

export const clearPermissions = () => {
    permissionSet = new Set();
    localStorage.removeItem(PERMISSIONS_KEY);
};

export const getPermissions = () => {
    return Array.from(permissionSet);
};

export const hasPermission = (permission: string) => {
    return permissionSet.has(permission);
};
