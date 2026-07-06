package com.thundax.kuzhambu.classics.infra.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baomidou.mybatisplus.annotation.TableField;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.assembler.ClassicsSharingPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsShareLinkDO;
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
}
