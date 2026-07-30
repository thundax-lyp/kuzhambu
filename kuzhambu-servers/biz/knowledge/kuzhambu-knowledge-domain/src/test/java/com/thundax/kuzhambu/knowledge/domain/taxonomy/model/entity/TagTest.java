package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TagTest {

    @Test
    void mergeIntoShouldMarkSourceAsMergedAndExcludeItFromNewBinding() {
        Tag sourceTag = enabledTag(1001L);
        Tag targetTag = enabledTag(1002L);

        sourceTag.mergeInto(targetTag);

        assertEquals(TagIdCodec.toDomain(1002L), sourceTag.getMergedToTagId());
        assertTrue(sourceTag.isMerged());
        assertFalse(sourceTag.isUsableForNewBinding());
        assertTrue(targetTag.isUsableForNewBinding());
    }

    @Test
    void mergeIntoShouldRejectInvalidSourceOrTarget() {
        Tag sourceTag = enabledTag(1001L);
        Tag mergedSourceTag = enabledTag(1003L);
        mergedSourceTag.setMergedToTagId(TagIdCodec.toDomain(1004L));
        Tag disabledTargetTag = enabledTag(1005L);
        disabledTargetTag.setStatus(TagStatus.DISABLED);

        assertThrows(DomainException.class, () -> sourceTag.mergeInto(sourceTag));
        assertThrows(DomainException.class, () -> sourceTag.mergeInto(disabledTargetTag));
        assertThrows(DomainException.class, () -> mergedSourceTag.mergeInto(enabledTag(1006L)));
    }

    @Test
    void deprecateShouldDisableTagAndExcludeItFromNewBinding() {
        Tag tag = enabledTag(1001L);

        tag.deprecate((Instant) null, 99L);

        assertTrue(tag.isDeprecated());
        assertEquals(TagStatus.DISABLED, tag.getStatus());
        assertNotNull(tag.getDeprecatedAt());
        assertEquals(99L, tag.getDeprecatedBy());
        assertFalse(tag.isUsableForNewBinding());
    }

    @Test
    void deprecateShouldRejectAlreadyDeprecatedTag() {
        Tag tag = enabledTag(1001L);
        tag.deprecate((Instant) null, null);

        assertThrows(DomainException.class, () -> tag.deprecate((Instant) null, null));
    }

    @Test
    void disabledTagShouldBeTreatedAsDeprecated() {
        Tag tag = enabledTag(1001L);
        tag.setStatus(TagStatus.DISABLED);

        assertTrue(tag.isDeprecated());
        assertFalse(tag.isUsableForNewBinding());
        assertThrows(DomainException.class, () -> tag.deprecate((Instant) null, null));
    }

    private static Tag enabledTag(Long tagId) {
        Tag tag = new Tag();
        tag.setId(TagIdCodec.toDomain(tagId));
        tag.setStatus(TagStatus.ENABLED);
        return tag;
    }
}
