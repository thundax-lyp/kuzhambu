import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type { OpenClientRecord, OpenClientSecretRecord } from "./open-client-types";

export type OpenClientStatus = "ENABLED" | "DISABLED";

export interface OpenClientPageQuery {
    pageNo?: number;
    pageSize?: number;
    name?: string | null;
    status?: OpenClientStatus | null;
}

export interface OpenClientSaveCommand {
    id?: string | null;
    name: string;
    ipWhitelist?: string | null;
    expiredAt?: string | null;
    remarks?: string | null;
    permissions?: string[] | null;
}

export interface OpenClientStatusCommand {
    id: string;
    status: OpenClientStatus;
}

export interface OpenClientSecretResetCommand {
    id: string;
}

export interface OpenClientIdCommand {
    id: string;
}

export const pageOpenClients = (request: OpenClientPageQuery = {}) => {
    return postJson<Page<OpenClientRecord>, OpenClientPageQuery>("/open/client/page", {
        body: request
    });
};

export const getOpenClient = (request: OpenClientIdCommand) => {
    return postJson<OpenClientRecord, OpenClientIdCommand>("/open/client/get", {
        body: request
    });
};

export const createOpenClient = (request: OpenClientSaveCommand) => {
    return postJson<OpenClientSecretRecord, OpenClientSaveCommand>("/open/client/create", {
        body: request
    });
};

export const changeOpenClientInfo = (request: OpenClientSaveCommand) => {
    return postJson<OpenClientRecord, OpenClientSaveCommand>("/open/client/update", {
        body: request
    });
};

export const changeOpenClientStatus = (request: OpenClientStatusCommand) => {
    return postJson<boolean, OpenClientStatusCommand>("/open/client/change-status", {
        body: request
    });
};

export const resetOpenClientSecret = (request: OpenClientSecretResetCommand) => {
    return postJson<OpenClientSecretRecord, OpenClientSecretResetCommand>(
        "/open/client/secret/reset",
        {
            body: request
        }
    );
};
