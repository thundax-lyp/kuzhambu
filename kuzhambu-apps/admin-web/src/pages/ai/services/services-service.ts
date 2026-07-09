import { postJson } from "@/api/http";
import type { AiServiceConfigRecord, AiServiceRole } from "./services-types";

export interface AiServiceConfigChangeCommand {
    serviceId: number;
    serviceRole: AiServiceRole;
    apiSource: string;
    baseUrl: string;
    encryptedApiKey?: string | null;
    enabled: boolean;
    status: string;
}

export const getServiceByRole = (serviceRole: string) => {
    return postJson<AiServiceConfigRecord, { serviceRole: AiServiceRole }>(
        "/ai/config/service/get-by-role",
        {
            body: { serviceRole: serviceRole as AiServiceRole }
        }
    );
};

export const changeServiceConfig = (request: AiServiceConfigChangeCommand) => {
    return postJson<AiServiceConfigRecord, AiServiceConfigChangeCommand>(
        "/ai/config/service/save",
        {
            body: request
        }
    );
};

export const listGovernanceServices = async () => {
    const [primary, backup] = await Promise.all([
        getServiceByRole("PRIMARY"),
        getServiceByRole("BACKUP")
    ]);
    return [primary, backup].filter((service): service is AiServiceConfigRecord =>
        Boolean(service?.serviceId)
    );
};
