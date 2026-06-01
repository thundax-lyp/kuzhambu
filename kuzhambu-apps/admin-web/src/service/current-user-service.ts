import { postJson } from "../api/http";
import type {
    CurrentUserMenuNode,
    CurrentUserPermsRecord,
    CurrentUserRecord
} from "./current-user-types";

export const getCurrentUserInfo = () => {
    return postJson<CurrentUserRecord>("/sys/current-user/info");
};

export const listCurrentUserMenus = () => {
    return postJson<CurrentUserMenuNode[]>("/sys/current-user/menus");
};

export const listCurrentUserPerms = () => {
    return postJson<CurrentUserPermsRecord>("/sys/current-user/perms");
};
