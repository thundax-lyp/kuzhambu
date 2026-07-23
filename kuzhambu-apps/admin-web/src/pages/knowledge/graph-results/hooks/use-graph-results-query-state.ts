import { useCallback, useMemo, useState } from "react";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import type {
    GraphEntityPageQuery,
    GraphLineageNodePageQuery,
    GraphLineageRelationPageQuery,
    GraphRelationPageQuery,
    GraphVersionPageQuery
} from "../graph-results-service";
import type {
    GraphVersionRecord
} from "../graph-results-types";

export type GraphResultsTabKey = "versions" | "entities" | "relations" | "lineage";

const readGraphVersionIdFromSearch = () => {
    if (typeof window === "undefined") {
        return null;
    }
    const versionId = Number(new URLSearchParams(window.location.search).get("graphVersionId"));
    return Number.isFinite(versionId) && versionId > 0 ? versionId : null;
};

export const useGraphResultsQueryState = () => {
    const [focusVersionId] = useState<number | null>(() => readGraphVersionIdFromSearch());
    const [activeTab, setActiveTab] = useState<GraphResultsTabKey>(
        focusVersionId ? "entities" : "versions"
    );
    const [versionQuery] = useState<GraphVersionPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [entityQuery, setEntityQuery] = useState<GraphEntityPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [relationQuery, setRelationQuery] = useState<GraphRelationPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [lineageNodeQuery, setLineageNodeQuery] = useState<GraphLineageNodePageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [lineageRelationQuery, setLineageRelationQuery] = useState<GraphLineageRelationPageQuery>(
        {
            pageNo: DEFAULT_PAGE_NO,
            pageSize: DEFAULT_PAGE_SIZE
        }
    );
    const [selectedVersion, setSelectedVersion] = useState<GraphVersionRecord | null>(null);

    const resolveActiveVersion = useCallback(
        (versions: GraphVersionRecord[]) => {
            const focusedVersion = focusVersionId
                ? versions.find((version) => version.versionId === focusVersionId) || null
                : null;
            return selectedVersion || focusedVersion;
        },
        [focusVersionId, selectedVersion]
    );

    const selectVersionResults = useCallback((version: GraphVersionRecord) => {
        setSelectedVersion(version);
        setEntityQuery((current) => ({
            ...current,
            pageNo: DEFAULT_PAGE_NO,
            versionId: version.versionId
        }));
        setRelationQuery((current) => ({
            ...current,
            pageNo: DEFAULT_PAGE_NO,
            versionId: version.versionId
        }));
        setLineageNodeQuery((current) => ({
            ...current,
            pageNo: DEFAULT_PAGE_NO,
            versionId: version.versionId
        }));
        setLineageRelationQuery((current) => ({
            ...current,
            pageNo: DEFAULT_PAGE_NO,
            versionId: version.versionId
        }));
        setActiveTab("entities");
    }, []);

    const activeVersionId = selectedVersion?.versionId || focusVersionId || null;
    const effectiveEntityQuery = useMemo(
        () => ({
            ...entityQuery,
            versionId: activeVersionId ?? entityQuery.versionId ?? null
        }),
        [activeVersionId, entityQuery]
    );
    const effectiveRelationQuery = useMemo(
        () => ({
            ...relationQuery,
            versionId: activeVersionId ?? relationQuery.versionId ?? null
        }),
        [activeVersionId, relationQuery]
    );
    const effectiveLineageNodeQuery = useMemo(
        () => ({
            ...lineageNodeQuery,
            versionId: activeVersionId ?? lineageNodeQuery.versionId ?? null
        }),
        [activeVersionId, lineageNodeQuery]
    );
    const effectiveLineageRelationQuery = useMemo(
        () => ({
            ...lineageRelationQuery,
            versionId: activeVersionId ?? lineageRelationQuery.versionId ?? null
        }),
        [activeVersionId, lineageRelationQuery]
    );

    return {
        activeTab,
        effectiveEntityQuery,
        effectiveLineageNodeQuery,
        effectiveLineageRelationQuery,
        effectiveRelationQuery,
        focusVersionId,
        resolveActiveVersion,
        selectVersionResults,
        setActiveTab,
        versionQuery
    };
};
