import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { createElement, useState, type PropsWithChildren } from "react";

const createQueryClient = () => {
    return new QueryClient({
        defaultOptions: {
            queries: {
                staleTime: 30 * 1000,
                refetchOnWindowFocus: false,
                retry: 1
            }
        }
    });
};

export const AdminQueryProvider = ({ children }: PropsWithChildren) => {
    const [client] = useState(createQueryClient);

    return createElement(QueryClientProvider, { client }, children);
};
