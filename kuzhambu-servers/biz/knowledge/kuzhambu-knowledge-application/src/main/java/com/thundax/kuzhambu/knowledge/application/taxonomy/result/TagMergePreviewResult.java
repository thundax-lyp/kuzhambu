package com.thundax.kuzhambu.knowledge.application.taxonomy.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagMergePreviewResult {
    private TagResult sourceTag;
    private TagResult targetTag;
    private List<TagAliasResult> aliasesToMerge;
    private List<TagContentRefResult> impactedContentRefs;
    private int pendingReviewCount;
    private int governedRecordCount;
}
