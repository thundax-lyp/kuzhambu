import { postJson } from "@/api/http";
import type { AiServiceConfigRecord, AiServiceRole } from "./services-types";

export interface AiServiceConfigChangeCommand {
    serviceId?: number | null;
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
    const [primary, backup, text2image] = await Promise.all([
        getServiceByRole("PRIMARY"),
        getServiceByRole("BACKUP"),
        getServiceByRole("TEXT2IMAGE")
    ]);
    return [primary, backup, text2image].filter((service): service is AiServiceConfigRecord =>
        Boolean(service?.serviceId)
    );
};
