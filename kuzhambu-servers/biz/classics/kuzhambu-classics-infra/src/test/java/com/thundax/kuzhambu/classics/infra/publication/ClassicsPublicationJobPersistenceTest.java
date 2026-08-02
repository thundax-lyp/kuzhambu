package com.thundax.kuzhambu.classics.infra.publication.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.annotation.TableField;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationCleanupStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.infra.publication.persistence.assembler.ClassicsPublicationPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.publication.persistence.dataobject.ClassicsPublicationJobDO;
import com.thundax.kuzhambu.classics.infra.publication.persistence.mapper.ClassicsPublicationJobMapper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Test;

class ClassicsPublicationJobPersistenceTest {

    @Test
    void shouldRoundTripPublicationJobWithoutLosingLeaseOrExternalReferences() {
        Instant now = Instant.ofEpochMilli(1_750_000_000_000L);
        ClassicsPublicationJob job = new ClassicsPublicationJob(
                new ClassicsPublicationJobId(11L),
                ClassicsPublicationJobType.PUBLISH,
                ClassicsContentType.SANCAI_ENTRY,
                101L,
                "title",
                null,
                ClassicsPublicationLifecycleStatus.DRAFT,
                ClassicsPublicationLifecycleStatus.PUBLISHED,
                201L,
                3,
                ClassicsPublicationJobStatus.ES_PREPARED,
                ClassicsPublicationJobResultStatus.RUNNING,
                new ClassicsPublicationExecutionToken("execution-token"),
                now.plusSeconds(30),
                null,
                1,
                4,
                "es-101",
                "collection-101",
                "[\"data-1\"]",
                ClassicsPublicationCleanupStatus.NONE,
                null,
                null,
                ClassicsPublicationCleanupStatus.NONE,
                null,
                null,
                "{\"provider\":\"ES\"}",
                now,
                now,
                null,
                null);

        ClassicsPublicationJob restored =
                ClassicsPublicationPersistenceAssembler.toDomain(ClassicsPublicationPersistenceAssembler.toObject(job));

        assertEquals(job.getId(), restored.getId());
        assertEquals(job.getExecutionToken(), restored.getExecutionToken());
        assertEquals(job.getJobStatus(), restored.getJobStatus());
        assertEquals(job.getEsDocumentId(), restored.getEsDocumentId());
        assertEquals(job.getFastGptDataIdsJson(), restored.getFastGptDataIdsJson());
    }

    @Test
    void conditionalUpdatesShouldRequireTheCurrentExecutionToken() throws Exception {
        assertTokenPredicate("markThreadStarted", Long.class, String.class, Instant.class, Instant.class);
        assertTokenPredicate(
                "advanceMilestone",
                Long.class,
                String.class,
                String.class,
                String.class,
                Long.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                String.class);
        Method advance = ClassicsPublicationJobMapper.class.getMethod(
                "advanceMilestone",
                Long.class,
                String.class,
                String.class,
                String.class,
                Long.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                String.class);
        String advanceSql = String.join(" ", advance.getAnnotation(Update.class).value());
        assertTrue(advanceSql.contains("content_version_id = coalesce(#{contentVersionId}, content_version_id)"));
        assertTrue(advanceSql.contains("content_version_no = coalesce(#{contentVersionNo}, content_version_no)"));
        assertTokenPredicate("bindFastGptCollection", Long.class, String.class, String.class, String.class);
        Method bindCollection = ClassicsPublicationJobMapper.class.getMethod(
                "bindFastGptCollection", Long.class, String.class, String.class, String.class);
        String bindCollectionSql =
                String.join(" ", bindCollection.getAnnotation(Update.class).value());
        assertTrue(bindCollectionSql.contains("job_status = #{expectedStatus}"));
        assertTrue(bindCollectionSql.contains("fastgpt_collection_id is null"));
        assertTokenPredicate("releaseForRetry", Long.class, String.class, Instant.class, String.class, String.class);
        assertTokenPredicate(
                "markTerminalFailure", Long.class, String.class, Instant.class, String.class, String.class);
        assertCleanupTokenPredicate("completeEsCleanup", "es_cleanup_token");
        assertCleanupTokenPredicate("failEsCleanup", "es_cleanup_token", String.class);
        assertCleanupTokenPredicate("completeFastGptCleanup", "fastgpt_cleanup_token");
        assertCleanupTokenPredicate("failFastGptCleanup", "fastgpt_cleanup_token", String.class);
    }

    @Test
    void shouldMapFastGptPropertiesToExistingDatabaseColumns() throws Exception {
        assertColumnMapping("fastGptCollectionId", "fastgpt_collection_id");
        assertColumnMapping("fastGptDataIdsJson", "fastgpt_data_ids_json");
        assertColumnMapping("fastGptCleanupStatus", "fastgpt_cleanup_status");
        assertColumnMapping("fastGptCleanupToken", "fastgpt_cleanup_token");
        assertColumnMapping("fastGptCleanupExpiresAt", "fastgpt_cleanup_expires_at");
    }

    @Test
    void contentDeletionShouldPreserveExternalReferencesForCleanup() throws Exception {
        Method method = ClassicsPublicationJobMapper.class.getMethod(
                "markContentDeleted", Long.class, String.class, Instant.class);
        String sql = String.join(" ", method.getAnnotation(Update.class).value());

        assertTrue(sql.contains("content_title_snapshot = #{contentTitleSnapshot}"));
        assertTrue(sql.contains("content_deleted_at = #{contentDeletedAt}"));
        assertTrue(sql.contains("es_document_id is null then es_cleanup_status else 'PENDING'"));
        assertTrue(sql.contains("fastgpt_collection_id is null then fastgpt_cleanup_status else 'PENDING'"));
        assertTrue(sql.contains("content_deleted_at is null"));
    }

    @Test
    void annotationSqlShouldUseNativeComparisonOperators() {
        for (Method method : ClassicsPublicationJobMapper.class.getDeclaredMethods()) {
            Update update = method.getAnnotation(Update.class);
            if (update == null) {
                continue;
            }
            String sql = String.join(" ", update.value());
            assertFalse(sql.contains("&lt;"), method.getName());
            assertFalse(sql.contains("&gt;"), method.getName());
        }
    }

    private static void assertTokenPredicate(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = ClassicsPublicationJobMapper.class.getMethod(methodName, parameterTypes);
        String sql = String.join(" ", method.getAnnotation(Update.class).value());
        assertTrue(sql.contains("execution_token = #{token}"), methodName);
        assertTrue(sql.contains("job_result_status = 'RUNNING'"), methodName);
    }

    private static void assertCleanupTokenPredicate(
            String methodName, String tokenColumn, Class<?>... remainingParameterTypes) throws Exception {
        Class<?>[] parameterTypes = new Class<?>[remainingParameterTypes.length + 2];
        parameterTypes[0] = Long.class;
        parameterTypes[1] = String.class;
        System.arraycopy(remainingParameterTypes, 0, parameterTypes, 2, remainingParameterTypes.length);
        Method method = ClassicsPublicationJobMapper.class.getMethod(methodName, parameterTypes);
        String sql = String.join(" ", method.getAnnotation(Update.class).value());
        assertTrue(sql.contains(tokenColumn + " = #{token}"), methodName);
    }

    private static void assertColumnMapping(String fieldName, String columnName) throws Exception {
        Field field = ClassicsPublicationJobDO.class.getDeclaredField(fieldName);
        assertEquals(columnName, field.getAnnotation(TableField.class).value());
    }
}
