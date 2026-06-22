package com.thundax.kuzhambu.knowledge.application.taxonomy.command;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagReviewCommand {
    private TagId id;
    private String decision;
    private String reviewNote;
}
