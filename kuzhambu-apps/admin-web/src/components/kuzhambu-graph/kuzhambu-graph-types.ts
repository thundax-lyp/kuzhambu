export interface KuzhambuGraphSpoItem {
    subject: string;
    predicate: string;
    object: string;
    group?: string;
    objectId?: string;
    subjectGroup?: string;
    subjectId?: string;
    objectGroup?: string;
}

export interface KuzhambuGraphNodeItem {
    id: string;
    label: string;
    group?: string;
}

export interface KuzhambuGraphHandle {
    appendSpoList: (spoList: KuzhambuGraphSpoItem[]) => Promise<void>;
}
