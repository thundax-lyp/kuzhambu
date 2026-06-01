import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { getAccessToken, subscribeAccessTokenChange } from "../auth/token-storage";

export const ProtectedRoute = () => {
    const location = useLocation();
    const [hasAccessToken, setHasAccessToken] = useState(() => Boolean(getAccessToken()));

    useEffect(() => {
        return subscribeAccessTokenChange(() => setHasAccessToken(Boolean(getAccessToken())));
    }, []);

    if (!hasAccessToken) {
        return <Navigate to="/login" replace state={{ from: location }} />;
    }

    return <Outlet />;
};
