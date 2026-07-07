package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

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
public class TagBatchDeprecateCommand {
    private List<TagId> tagIds;
}
