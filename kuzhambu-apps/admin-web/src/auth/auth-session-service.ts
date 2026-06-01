import { login } from "./auth-service";
import type { LoginCommand } from "./auth-service";
import type { AccessTokenRecord } from "./auth-types";
import { listCurrentUserPerms } from "../service/current-user-service";
import { clearPermissions, replacePermissions } from "./permission-storage";
import { clearAccessToken, saveTokenSession } from "./token-storage";

export const loginWithPermissions = async (request: LoginCommand): Promise<AccessTokenRecord> => {
    clearPermissions();
    const response = await login(request);
    saveTokenSession(response);

    try {
        const currentUserPerms = await listCurrentUserPerms();
        replacePermissions(currentUserPerms.perms || []);
    } catch (error) {
        clearAccessToken();
        throw error;
    }

    return response;
};
