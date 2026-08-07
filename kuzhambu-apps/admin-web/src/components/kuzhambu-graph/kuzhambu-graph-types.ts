export interface KuzhambuGraphSpoItem {
    subject: string;
    predicate: string;
    object: string;
    group?: string;
    subjectGroup?: string;
    objectGroup?: string;
}

export interface KuzhambuGraphHandle {
    appendSpoList: (spoList: KuzhambuGraphSpoItem[]) => Promise<void>;
}
