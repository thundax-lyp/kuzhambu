package com.thundax.kuzhambu.knowledge.application.taxonomy.query;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagBatchMergePreviewQuery {
    private List<TagId> sourceTagIds;
    private TagId targetTagId;
}
