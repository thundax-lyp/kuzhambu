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
public class TagDetailResult {
    private TagResult tag;
    private List<TagAliasResult> aliases;
    private List<TagContentRefResult> contentRefs;
}
