import { Avatar } from "antd";
import { useCurrentAccessToken } from "@/auth/hooks/use-current-access-token";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import type { UserRecord } from "./user-types";

interface UserAvatarProps {
    user: UserRecord;
    size?: number;
}

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const getInitials = (name?: string | null) => {
    const normalizedName = normalizeSearch(name) || "U";
    return Array.from(normalizedName.replace(/\s+/g, "")).slice(0, 2).join("");
};

const readUserName = (user: UserRecord) => {
    return normalizeSearch(user.name) || normalizeSearch(user.loginName) || `用户 ${user.id}`;
};

export const UserAvatar = ({ user, size }: UserAvatarProps) => {
    const accessToken = useCurrentAccessToken();
    const userName = readUserName(user);

    return (
        <Avatar size={size} src={toAuthenticatedResourceUrl(user.avatar, accessToken)}>
            {user.avatar ? null : getInitials(userName)}
        </Avatar>
    );
};
