import { useEffect, useState } from "react";
import { getAccessToken, subscribeAccessTokenChange } from "../token-storage";

export const useCurrentAccessToken = () => {
    const [accessToken, setAccessToken] = useState(() => getAccessToken());

    useEffect(() => {
        return subscribeAccessTokenChange(() => setAccessToken(getAccessToken()));
    }, []);

    return accessToken;
};
