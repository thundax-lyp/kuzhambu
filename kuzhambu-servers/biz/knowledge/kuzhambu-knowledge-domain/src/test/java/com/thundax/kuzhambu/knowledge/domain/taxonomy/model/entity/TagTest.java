package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import org.junit.jupiter.api.Test;

class TagTest {

    @Test
    void mergeIntoShouldMarkSourceAsMergedAndExcludeItFromNewBinding() {
        Tag sourceTag = enabledTag(1001L);
        Tag targetTag = enabledTag(1002L);

        sourceTag.mergeInto(targetTag);

        assertEquals(TagId.of(1002L), sourceTag.getMergedToTagId());
        assertTrue(sourceTag.isMerged());
        assertFalse(sourceTag.isUsableForNewBinding());
        assertTrue(targetTag.isUsableForNewBinding());
    }

    @Test
    void mergeIntoShouldRejectInvalidSourceOrTarget() {
        Tag sourceTag = enabledTag(1001L);
        Tag mergedSourceTag = enabledTag(1003L);
        mergedSourceTag.setMergedToTagId(TagId.of(1004L));
        Tag disabledTargetTag = enabledTag(1005L);
        disabledTargetTag.setStatus(TagStatus.DISABLED);

        assertThrows(DomainException.class, () -> sourceTag.mergeInto(sourceTag));
        assertThrows(DomainException.class, () -> sourceTag.mergeInto(disabledTargetTag));
        assertThrows(DomainException.class, () -> mergedSourceTag.mergeInto(enabledTag(1006L)));
    }

    private static Tag enabledTag(Long tagId) {
        Tag tag = new Tag();
        tag.setTagId(TagId.of(tagId));
        tag.setStatus(TagStatus.ENABLED);
        return tag;
    }
}
