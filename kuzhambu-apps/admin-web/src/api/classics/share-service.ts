import { postJson } from "@/api/http";
import type {
    ClassicsShareLinkStatus,
    ClassicsShareRecord,
    ClassicsShareTargetRef,
    ClassicsShareVisibility
} from "@/service/classics-share-types";

export interface ClassicsShareCreateCommand {
    expiresAt?: string | null;
    status?: ClassicsShareLinkStatus | null;
    targets: ClassicsShareTargetRef[];
    title?: string | null;
    visibility?: ClassicsShareVisibility | null;
}

export const create = (request: ClassicsShareCreateCommand) => {
    return postJson<ClassicsShareRecord, ClassicsShareCreateCommand>("/classics/shares/create", {
        body: request
    });
};
