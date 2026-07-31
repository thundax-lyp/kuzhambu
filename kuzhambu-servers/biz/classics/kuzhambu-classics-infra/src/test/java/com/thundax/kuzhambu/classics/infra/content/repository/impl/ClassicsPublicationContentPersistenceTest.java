package com.thundax.kuzhambu.classics.infra.content.repository.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper.MingCustomsEntryMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiMapper;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.mapper.WangqiDocumentMapper;
import java.lang.reflect.Method;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Test;

class ClassicsPublicationContentPersistenceTest {

    @Test
    void allContentTypesShouldLockRowsBeforePublicationChanges() throws Exception {
        assertRowLock(SancaiMapper.class);
        assertRowLock(WangqiDocumentMapper.class);
        assertRowLock(MingCustomsEntryMapper.class);
    }

    @Test
    void allContentTypesShouldUseExpectedPublicationStateForUpdates() throws Exception {
        assertConditionalUpdate(SancaiMapper.class);
        assertConditionalUpdate(WangqiDocumentMapper.class);
        assertConditionalUpdate(MingCustomsEntryMapper.class);
    }

    private static void assertRowLock(Class<?> mapperType) throws Exception {
        Method method = mapperType.getMethod("selectPublicationStateForUpdate", Long.class);
        String sql =
                String.join(" ", method.getAnnotation(Select.class).value()).toLowerCase();
        assertTrue(sql.contains("for update"), mapperType.getSimpleName());
    }

    private static void assertConditionalUpdate(Class<?> mapperType) throws Exception {
        Method method = mapperType.getMethod(
                "updatePublicationState",
                Long.class,
                String.class,
                String.class,
                Long.class,
                String.class,
                String.class,
                Long.class);
        String sql = String.join(" ", method.getAnnotation(Update.class).value());
        assertTrue(sql.contains("lifecycle_status = #{expectedLifecycleStatus}"), mapperType.getSimpleName());
        assertTrue(sql.contains("transition_status = #{expectedTransitionStatus}"), mapperType.getSimpleName());
        assertTrue(sql.contains("current_publication_job_id = #{expectedJobId}"), mapperType.getSimpleName());
    }
}
