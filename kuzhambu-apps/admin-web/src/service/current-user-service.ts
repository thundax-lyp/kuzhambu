import { postJson } from "../api/http";
import type {
    CurrentUserMenuNode,
    CurrentUserPermsRecord,
    CurrentUserRecord
} from "./current-user-types";

export const getCurrentUserInfo = () => {
    return postJson<CurrentUserRecord>("/sys/current-user/get");
};

export const listCurrentUserMenus = () => {
    return postJson<CurrentUserMenuNode[]>("/sys/current-user/menu/list");
};

export const listCurrentUserPerms = () => {
    return postJson<CurrentUserPermsRecord>("/sys/current-user/permission/list");
};
