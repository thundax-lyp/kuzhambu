import { postJson } from "@/api/http";
import type {
    ClassicsPublicationContentType,
    ClassicsPublicationJobRecord,
    ClassicsPublicationJobResultStatus,
    ClassicsPublicationJobStatus,
    ClassicsPublicationJobType
} from "@/pages/classics/publication-jobs/publication-jobs-types";
import type { Page, PageQuery } from "@/types/page";

export type ClassicsPublicationJobQuery = PageQuery<{
    jobType?: ClassicsPublicationJobType | null;
    jobResultStatus?: ClassicsPublicationJobResultStatus | null;
    jobStatus?: ClassicsPublicationJobStatus | null;
    contentType?: ClassicsPublicationContentType | null;
    keyword?: string | null;
}>;

export interface ClassicsPublicationJobGetCommand {
    id: string;
}

const PUBLICATION_JOBS_PATH = "/classics/publication-jobs";

export const page = (query: ClassicsPublicationJobQuery = {}) => {
    return postJson<Page<ClassicsPublicationJobRecord>, ClassicsPublicationJobQuery>(
        `${PUBLICATION_JOBS_PATH}/page`,
        { body: query }
    );
};

export const get = (command: ClassicsPublicationJobGetCommand) => {
    return postJson<ClassicsPublicationJobRecord, ClassicsPublicationJobGetCommand>(
        `${PUBLICATION_JOBS_PATH}/get`,
        { body: command }
    );
};
