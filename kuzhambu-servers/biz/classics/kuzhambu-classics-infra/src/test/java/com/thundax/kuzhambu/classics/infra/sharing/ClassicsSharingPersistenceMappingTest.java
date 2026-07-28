package com.thundax.kuzhambu.classics.infra.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baomidou.mybatisplus.annotation.TableField;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareTargetIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareTargetStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.assembler.ClassicsSharingPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsShareLinkDO;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsShareTargetDO;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class ClassicsSharingPersistenceMappingTest {

    @Test
    void linkMappingShouldCarryTokenHash() throws NoSuchFieldException {
        ClassicsShareLink link = new ClassicsShareLink(
                null,
                "share-token",
                "hashed-share-token",
                "分享",
                ClassicsShareVisibility.PUBLIC,
                ClassicsShareLinkStatus.ACTIVE,
                null,
                1001L,
                null,
                null,
                0L);

        ClassicsShareLinkDO dataObject = ClassicsSharingPersistenceAssembler.toLinkObject(link);
        Field shareTokenField = ClassicsShareLinkDO.class.getDeclaredField("shareToken");
        Field tokenHashField = ClassicsShareLinkDO.class.getDeclaredField("tokenHash");

        assertEquals("share-token", dataObject.getShareToken());
        assertEquals("hashed-share-token", dataObject.getTokenHash());
        assertEquals(1001L, dataObject.getCreatedByUserId());
        assertEquals(
                "share_token", shareTokenField.getAnnotation(TableField.class).value());
        assertEquals(
                "token_hash", tokenHashField.getAnnotation(TableField.class).value());
    }

    @Test
    void targetMappingShouldCarryContentDeletedStatus() {
        ClassicsShareTargetDO dataObject = new ClassicsShareTargetDO(
                20L, 10L, "SANCAI_ENTRY", 100L, 30L, 3, "已删标题", "{\"title\":\"已删标题\"}", "PUBLIC", "CONTENT_DELETED", 1);

        ClassicsShareTarget target = ClassicsSharingPersistenceAssembler.toTargetDomain(dataObject);
        ClassicsShareTargetDO mappedDataObject = ClassicsSharingPersistenceAssembler.toTargetObject(target);

        assertEquals(ClassicsShareTargetIdCodec.toDomain(20L), target.getId());
        assertEquals(ClassicsContentType.SANCAI_ENTRY, target.getContentType());
        assertEquals(ClassicsShareTargetStatus.CONTENT_DELETED, target.getTargetStatus());
        assertEquals("CONTENT_DELETED", mappedDataObject.getTargetStatus());
    }
}
