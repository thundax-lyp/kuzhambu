import {
    clearPermissions,
    hasPermission,
    replacePermissions,
    subscribePermissionsChange
} from "./permission-storage";

describe("permission-storage", () => {
    beforeEach(() => {
        localStorage.clear();
        clearPermissions();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("reloads permissions from localStorage when another tab updates storage", () => {
        replacePermissions(["knowledge:graph:view"]);
        const listener = vi.fn();
        const unsubscribe = subscribePermissionsChange(listener);

        localStorage.setItem(
            "kuzhambu.admin.permissions",
            JSON.stringify(["knowledge:graph:view", "knowledge:graph:apply"])
        );
        window.dispatchEvent(
            new StorageEvent("storage", {
                key: "kuzhambu.admin.permissions"
            })
        );

        expect(listener).toHaveBeenCalledTimes(1);
        expect(hasPermission("knowledge:graph:apply")).toBe(true);

        unsubscribe();
    });
});
