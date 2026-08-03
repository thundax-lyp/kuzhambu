package com.thundax.kuzhambu.starter.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationCreateCommand;
import com.thundax.kuzhambu.classics.application.publication.scheduler.ClassicsPublicationDispatchScheduler;
import com.thundax.kuzhambu.classics.application.publication.scheduler.ClassicsPublicationEsCleanupScheduler;
import com.thundax.kuzhambu.classics.application.publication.scheduler.ClassicsPublicationFailureReconcileScheduler;
import com.thundax.kuzhambu.classics.application.publication.scheduler.ClassicsPublicationFastGptCleanupScheduler;
import com.thundax.kuzhambu.classics.application.publication.scheduler.ClassicsPublicationSuccessReconcileScheduler;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseEnsureRequest;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseListRequest;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBasePageResult;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseResult;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatRequest;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatResult;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatStreamHandler;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionCreateRequest;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionReferenceRequest;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionResult;
import com.thundax.kuzhambu.common.knowledge.model.collection.KnowledgeCollectionUpdateRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataListRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPageResult;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPushRequest;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataPushResult;
import com.thundax.kuzhambu.common.knowledge.model.data.KnowledgeCollectionDataReferenceRequest;
import com.thundax.kuzhambu.common.knowledge.model.health.KnowledgeHealthResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemDeleteRequest;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemListRequest;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemPageResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemUpsertRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncResult;
import com.thundax.kuzhambu.discovery.facade.DiscoverySearchPublicationFacade;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationCandidatePageFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationCategoryAggregationFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationPrepareFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationReferenceFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationCandidatePageFacadeResponse;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationCategoryAggregationFacadeResponse;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationProbeFacadeResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
        classes = {KuzhambuAdminApplication.class, ClassicsPublicationRuntimeSmokeIT.SmokeConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "kuzhambu.classics.publication.enabled=true",
            "kuzhambu.classics.publication.dispatch-fixed-delay=86400000",
            "kuzhambu.classics.publication.success-reconcile-fixed-delay=86400000",
            "kuzhambu.classics.publication.failure-reconcile-fixed-delay=86400000",
            "kuzhambu.classics.publication.es-cleanup-fixed-delay=86400000",
            "kuzhambu.classics.publication.fastgpt-cleanup-fixed-delay=86400000",
            "kuzhambu.classics.publication.executor-core-size=1",
            "kuzhambu.classics.publication.executor-max-size=1",
            "kuzhambu.classics.publication.executor-queue-capacity=0",
            "kuzhambu.classics.publication.retry-delay=30s"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ClassicsPublicationRuntimeSmokeIT {
    private static final long CONTENT_ID = 990000009900L;
    private static final long VERSION_ID = 990000009901L;
    private static final Instant NOW = Instant.parse("2026-08-03T02:00:00Z");
    private static final String TITLE = "publication-runtime-smoke";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ClassicsPublicationApplicationService publicationService;

    @Autowired
    private MingCustomsApplicationService mingCustomsService;

    @Autowired
    private ClassicsPublicationDispatchScheduler dispatchScheduler;

    @Autowired
    private ClassicsPublicationSuccessReconcileScheduler successScheduler;

    @Autowired
    private ClassicsPublicationFailureReconcileScheduler failureScheduler;

    @Autowired
    private ClassicsPublicationEsCleanupScheduler esCleanupScheduler;

    @Autowired
    private ClassicsPublicationFastGptCleanupScheduler fastGptCleanupScheduler;

    @Autowired
    @Qualifier("classicsPublicationTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private MutableClock clock;

    @Autowired
    private FakeDiscoveryPublicationFacade discovery;

    @Autowired
    private FakeKnowledgeBaseClient knowledge;

    @BeforeEach
    void prepare() {
        restore();
        clock.set(NOW);
        discovery.reset();
        knowledge.reset();
        insertFixture();
    }

    @AfterEach
    void restore() {
        jdbcTemplate.update(
                "delete from classics_publication_job where content_type = 'MING_CUSTOMS' and content_id = ?",
                CONTENT_ID);
        jdbcTemplate.update(
                "delete from classics_content_version where content_type = 'MING_CUSTOMS' and content_id = ?",
                CONTENT_ID);
        jdbcTemplate.update(
                "delete from classics_content_tag where content_type = 'MING_CUSTOMS' and content_id = ?", CONTENT_ID);
        jdbcTemplate.update(
                "delete from classics_content_qa_pair where content_type = 'MING_CUSTOMS' and content_id = ?",
                CONTENT_ID);
        jdbcTemplate.update("delete from classics_ming_customs_keyword where custom_id = ?", CONTENT_ID);
        jdbcTemplate.update("delete from classics_ming_customs_entry where id = ?", CONTENT_ID);
    }

    @Test
    void firstStepFailureShouldReleaseLeaseAndScheduleThirtySecondRetry() {
        long jobId = createPreparedPublishJob();
        discovery.failNextPrepares(1);

        dispatchScheduler.dispatch();

        awaitJobValue(jobId, "next_retry_at", NOW.plusSeconds(30).toEpochMilli());
        assertJob(jobId, "SNAPSHOT_READY", "RUNNING", 1);
        assertThat(jobValue(jobId, "execution_token")).isNull();
        assertThat(jobValue(jobId, "expires_at")).isNull();
    }

    @Test
    void retryDueShouldUseNewTokenAndContinueFromFailedMilestone() {
        long jobId = createPreparedPublishJob();
        discovery.failNextPrepares(1);
        dispatchScheduler.dispatch();
        awaitJobValue(jobId, "next_retry_at", NOW.plusSeconds(30).toEpochMilli());

        clock.advance(Duration.ofSeconds(31));
        dispatchScheduler.dispatch();

        awaitJobValue(jobId, "job_status", "ES_PREPARED");
        assertThat(discovery.executionTokens()).hasSize(2).doesNotHaveDuplicates();
        assertThat(discovery.prepareCalls()).isEqualTo(2);
        assertJob(jobId, "ES_PREPARED", "RUNNING", 0);
    }

    @Test
    void expiredExecutionLeaseShouldBeClaimedAndAdvanced() {
        long jobId = createPreparedPublishJob();
        jdbcTemplate.update(
                "update classics_publication_job set execution_token = 'expired-token', expires_at = ? where id = ?",
                NOW.minusSeconds(1).toEpochMilli(),
                jobId);

        dispatchScheduler.dispatch();

        awaitJobValue(jobId, "job_status", "ES_PREPARED");
        assertThat(discovery.executionTokens()).singleElement().isNotEqualTo("expired-token");
        assertJob(jobId, "ES_PREPARED", "RUNNING", 0);
    }

    @Test
    void fourthFailureShouldFailJobAndReconcileContentToError() {
        long jobId = createPreparedPublishJob();
        discovery.failNextPrepares(4);

        for (int attempt = 1; attempt <= 4; attempt++) {
            dispatchScheduler.dispatch();
            if (attempt < 4) {
                awaitJobValue(
                        jobId, "next_retry_at", clock.instant().plusSeconds(30).toEpochMilli());
                clock.advance(Duration.ofSeconds(31));
            }
        }

        awaitJobValue(jobId, "job_result_status", "FAILED");
        assertJob(jobId, "SNAPSHOT_READY", "FAILED", 4);
        failureScheduler.reconcile();
        assertContent("ERROR", "NONE", null);
    }

    @Test
    void errorRepublishShouldReplaceOldJobAndOnlyInheritExternalReferences() {
        long oldJobId = createPublishJob();
        jdbcTemplate.update(
                "update classics_publication_job set job_result_status = 'FAILED', es_document_id = 'old-es', "
                        + "fastgpt_collection_id = 'old-fastgpt', es_cleanup_status = 'PENDING', "
                        + "fastgpt_cleanup_status = 'FAILED' where id = ?",
                oldJobId);
        jdbcTemplate.update(
                "update classics_ming_customs_entry set lifecycle_status = 'ERROR', transition_status = 'NONE', "
                        + "current_publication_job_id = null where id = ?",
                CONTENT_ID);

        long newJobId = createPublishJob();

        assertThat(newJobId).isNotEqualTo(oldJobId);
        assertThat(jobCount(oldJobId)).isZero();
        assertJob(newJobId, "QUEUED", "RUNNING", 0);
        assertThat(jobValue(newJobId, "es_document_id")).isEqualTo("old-es");
        assertThat(jobValue(newJobId, "fastgpt_collection_id")).isEqualTo("old-fastgpt");
        assertThat(jobValue(newJobId, "es_cleanup_status")).isEqualTo("NONE");
        assertThat(jobValue(newJobId, "fastgpt_cleanup_status")).isEqualTo("NONE");
    }

    @Test
    void contentCommittedJobShouldBeClosedBySuccessReconcile() {
        long jobId = createPublishJob();
        jdbcTemplate.update(
                "update classics_ming_customs_entry set lifecycle_status = 'PUBLISHED', transition_status = 'NONE', "
                        + "current_publication_job_id = null where id = ?",
                CONTENT_ID);
        jdbcTemplate.update("update classics_publication_job set job_status = 'CONTENT_COMMITTED' where id = ?", jobId);

        successScheduler.reconcile();

        assertJob(jobId, "CONTENT_COMMITTED", "SUCCEEDED", 0);
        assertThat(number(jobValue(jobId, "finished_at"))).isEqualTo(NOW.toEpochMilli());
    }

    @Test
    void poolRejectionShouldReleaseLeaseWithoutIncreasingAttempt() {
        long jobId = createPreparedPublishJob();
        CountDownLatch release = new CountDownLatch(1);
        taskExecutor.execute(() -> awaitLatch(release));
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(taskExecutor.getActiveCount()).isOne());

        dispatchScheduler.dispatch();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(jobValue(jobId, "execution_token")).isNull();
            assertThat(number(jobValue(jobId, "attempt_count"))).isZero();
        });
        release.countDown();
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(taskExecutor.getActiveCount()).isZero());
    }

    @Test
    void deletedErrorContentShouldRetainTombstoneUntilBothCleanupsSucceed() {
        long jobId = createPublishJob();
        jdbcTemplate.update(
                "update classics_publication_job set job_result_status = 'FAILED', es_document_id = 'delete-es', "
                        + "fastgpt_collection_id = 'delete-fastgpt' where id = ?",
                jobId);
        jdbcTemplate.update(
                "update classics_ming_customs_entry set lifecycle_status = 'ERROR', transition_status = 'NONE', "
                        + "current_publication_job_id = null where id = ?",
                CONTENT_ID);
        knowledge.putCollection("delete-fastgpt", false);

        mingCustomsService.delete(new MingCustomsEntryId(CONTENT_ID));

        assertThat(contentCount()).isZero();
        assertThat(jobCount(jobId)).isOne();
        assertThat(jobValue(jobId, "content_deleted_at")).isNotNull();
        assertThat(jobValue(jobId, "es_cleanup_status")).isEqualTo("PENDING");
        assertThat(jobValue(jobId, "fastgpt_cleanup_status")).isEqualTo("PENDING");

        esCleanupScheduler.cleanup();
        fastGptCleanupScheduler.cleanup();

        assertThat(jobValue(jobId, "es_cleanup_status")).isEqualTo("SUCCEEDED");
        assertThat(jobValue(jobId, "fastgpt_cleanup_status")).isEqualTo("SUCCEEDED");
        assertThat(jobValue(jobId, "es_document_id")).isNull();
        assertThat(jobValue(jobId, "fastgpt_collection_id")).isNull();
        assertThat(discovery.deletedDocuments()).containsExactly("delete-es");
        assertThat(knowledge.deletedCollections()).containsExactly("delete-fastgpt");
    }

    private void insertFixture() {
        jdbcTemplate.update(
                "insert into classics_ming_customs_entry "
                        + "(id, title, category, chapter, section, summary, content_format, content, original_excerpts, "
                        + "lifecycle_status, transition_status, current_publication_job_id, current_version_id, "
                        + "current_version_no, current_versioned_at, content_updated_at) "
                        + "values (?, ?, 'SMOKE', 'SMOKE', 'SMOKE', 'runtime smoke', 'MARKDOWN', 'smoke content', "
                        + "'smoke excerpt', 'DRAFT', 'NONE', null, ?, 1, ?, ?)",
                CONTENT_ID,
                TITLE,
                VERSION_ID,
                NOW.toEpochMilli(),
                NOW.toEpochMilli());
        jdbcTemplate.update(
                "insert into classics_content_version "
                        + "(id, content_type, content_id, version_no, versioned_at, snapshot_json, change_type, change_summary) "
                        + "values (?, 'MING_CUSTOMS', ?, 1, ?, ?, 'MANUAL_SAVE', 'runtime smoke fixture')",
                VERSION_ID,
                CONTENT_ID,
                NOW.toEpochMilli(),
                snapshotJson());
    }

    private long createPreparedPublishJob() {
        long jobId = createPublishJob();
        jdbcTemplate.update(
                "update classics_publication_job set job_status = 'SNAPSHOT_READY', content_version_id = ?, "
                        + "content_version_no = 1 where id = ?",
                VERSION_ID,
                jobId);
        return jobId;
    }

    private long createPublishJob() {
        return publicationService
                .create(new ClassicsPublicationCreateCommand(
                        ClassicsContentType.MING_CUSTOMS,
                        new ClassicsContentId(CONTENT_ID),
                        ClassicsPublicationJobType.PUBLISH))
                .jobId()
                .value();
    }

    private void assertJob(long jobId, String status, String result, int attempts) {
        assertThat(jobValue(jobId, "job_status")).isEqualTo(status);
        assertThat(jobValue(jobId, "job_result_status")).isEqualTo(result);
        assertThat(number(jobValue(jobId, "attempt_count"))).isEqualTo(attempts);
    }

    private void assertContent(String lifecycle, String transition, Long currentJobId) {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select lifecycle_status, transition_status, current_publication_job_id "
                        + "from classics_ming_customs_entry where id = ?",
                CONTENT_ID);
        assertThat(row.get("lifecycle_status")).isEqualTo(lifecycle);
        assertThat(row.get("transition_status")).isEqualTo(transition);
        assertThat(row.get("current_publication_job_id")).isEqualTo(currentJobId);
    }

    private void awaitJobValue(long jobId, String column, Object expected) {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Object actual = jobValue(jobId, column);
            if (actual instanceof Number && expected instanceof Number) {
                assertThat(((Number) actual).longValue()).isEqualTo(((Number) expected).longValue());
            } else {
                assertThat(actual).isEqualTo(expected);
            }
        });
    }

    private Object jobValue(long jobId, String column) {
        return jdbcTemplate.queryForObject(
                "select " + column + " from classics_publication_job where id = ?", Object.class, jobId);
    }

    private int jobCount(long jobId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from classics_publication_job where id = ?", Integer.class, jobId);
    }

    private int contentCount() {
        return jdbcTemplate.queryForObject(
                "select count(*) from classics_ming_customs_entry where id = ?", Integer.class, CONTENT_ID);
    }

    private static long number(Object value) {
        return ((Number) value).longValue();
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private static String snapshotJson() {
        return """
                {"contentType":"MING_CUSTOMS","contentId":990000009900,
                "contentUpdatedAt":"2026-08-03T02:00:00Z","title":"publication-runtime-smoke",
                "category":"SMOKE","chapter":"SMOKE","section":"SMOKE","summary":"runtime smoke",
                "contentFormat":"MARKDOWN","content":"smoke content","originalExcerpts":"smoke excerpt",
                "lifecycleStatus":"DRAFT","tags":[],"qaPairs":[]}
                """;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class SmokeConfiguration {
        @Bean
        @Primary
        MutableClock publicationSmokeClock() {
            return new MutableClock(NOW);
        }

        @Bean
        @Primary
        FakeDiscoveryPublicationFacade fakeDiscoveryPublicationFacade(JdbcTemplate jdbcTemplate) {
            return new FakeDiscoveryPublicationFacade(jdbcTemplate);
        }

        @Bean
        @Primary
        FakeKnowledgeBaseClient fakeKnowledgeBaseClient() {
            return new FakeKnowledgeBaseClient();
        }
    }

    static final class MutableClock extends Clock {
        private final AtomicReference<Instant> instant;

        MutableClock(Instant instant) {
            this.instant = new AtomicReference<>(instant);
        }

        void set(Instant value) {
            instant.set(value);
        }

        void advance(Duration duration) {
            instant.updateAndGet(value -> value.plus(duration));
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant.get();
        }
    }

    static final class FakeDiscoveryPublicationFacade implements DiscoverySearchPublicationFacade {
        private final JdbcTemplate jdbcTemplate;
        private final AtomicInteger prepareFailures = new AtomicInteger();
        private final AtomicInteger prepareCalls = new AtomicInteger();
        private final List<String> executionTokens = new CopyOnWriteArrayList<>();
        private final List<String> deletedDocuments = new CopyOnWriteArrayList<>();

        FakeDiscoveryPublicationFacade(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        void reset() {
            prepareFailures.set(0);
            prepareCalls.set(0);
            executionTokens.clear();
            deletedDocuments.clear();
        }

        void failNextPrepares(int count) {
            prepareFailures.set(count);
        }

        int prepareCalls() {
            return prepareCalls.get();
        }

        List<String> executionTokens() {
            return List.copyOf(executionTokens);
        }

        List<String> deletedDocuments() {
            return List.copyOf(deletedDocuments);
        }

        @Override
        public void prepare(DiscoverySearchPublicationPrepareFacadeRequest request) {
            prepareCalls.incrementAndGet();
            executionTokens.add(jdbcTemplate.queryForObject(
                    "select execution_token from classics_publication_job "
                            + "where content_type = 'MING_CUSTOMS' and content_id = ?",
                    String.class,
                    CONTENT_ID));
            if (prepareFailures.getAndUpdate(value -> Math.max(0, value - 1)) > 0) {
                throw new IllegalStateException("simulated discovery failure");
            }
        }

        @Override
        public void markReady(DiscoverySearchPublicationReferenceFacadeRequest request) {}

        @Override
        public void markOffline(DiscoverySearchPublicationReferenceFacadeRequest request) {}

        @Override
        public void delete(DiscoverySearchPublicationReferenceFacadeRequest request) {
            deletedDocuments.add(request.getDocumentId());
        }

        @Override
        public DiscoverySearchPublicationProbeFacadeResponse probe(
                DiscoverySearchPublicationReferenceFacadeRequest request) {
            return null;
        }

        @Override
        public DiscoverySearchPublicationCandidatePageFacadeResponse pageReadyCandidates(
                DiscoverySearchPublicationCandidatePageFacadeRequest request) {
            return null;
        }

        @Override
        public List<DiscoverySearchPublicationCategoryAggregationFacadeResponse> listReadyCandidateCategoryAggregations(
                DiscoverySearchPublicationCategoryAggregationFacadeRequest request) {
            return List.of();
        }
    }

    static final class FakeKnowledgeBaseClient implements KnowledgeBaseClient {
        private final Map<String, Boolean> collections = new ConcurrentHashMap<>();
        private final Set<String> deletedCollections = ConcurrentHashMap.newKeySet();
        private final AtomicInteger sequence = new AtomicInteger();

        void reset() {
            collections.clear();
            deletedCollections.clear();
            sequence.set(0);
        }

        void putCollection(String collectionId, boolean forbid) {
            collections.put(collectionId, forbid);
        }

        Set<String> deletedCollections() {
            return Set.copyOf(deletedCollections);
        }

        @Override
        public KnowledgeCollectionResult createCollection(KnowledgeCollectionCreateRequest request) {
            String id = "smoke-collection-" + sequence.incrementAndGet();
            collections.put(id, true);
            return new KnowledgeCollectionResult(id, true, Map.of());
        }

        @Override
        public KnowledgeCollectionResult getCollection(KnowledgeCollectionReferenceRequest request) {
            Boolean forbid = collections.get(request.collectionId());
            return forbid == null ? null : new KnowledgeCollectionResult(request.collectionId(), forbid, Map.of());
        }

        @Override
        public void updateCollection(KnowledgeCollectionUpdateRequest request) {
            if (collections.containsKey(request.collectionId())) {
                collections.put(request.collectionId(), request.forbid());
            }
        }

        @Override
        public void deleteCollection(KnowledgeCollectionReferenceRequest request) {
            collections.remove(request.collectionId());
            deletedCollections.add(request.collectionId());
        }

        @Override
        public KnowledgeCollectionDataPageResult listCollectionData(KnowledgeCollectionDataListRequest request) {
            return new KnowledgeCollectionDataPageResult(0, List.of(), Map.of());
        }

        @Override
        public void deleteCollectionData(KnowledgeCollectionDataReferenceRequest request) {}

        @Override
        public KnowledgeCollectionDataPushResult pushCollectionData(KnowledgeCollectionDataPushRequest request) {
            return new KnowledgeCollectionDataPushResult(request.data().size(), Map.of());
        }

        @Override
        public KnowledgeHealthResult health() {
            return null;
        }

        @Override
        public KnowledgeBasePageResult listKnowledgeBases(KnowledgeBaseListRequest request) {
            return null;
        }

        @Override
        public KnowledgeBaseResult ensureKnowledgeBase(KnowledgeBaseEnsureRequest request) {
            return null;
        }

        @Override
        public KnowledgeItemPageResult listKnowledgeItems(KnowledgeItemListRequest request) {
            return null;
        }

        @Override
        public KnowledgeItemResult upsertKnowledgeItem(KnowledgeItemUpsertRequest request) {
            return null;
        }

        @Override
        public KnowledgeSyncResult syncKnowledgeItem(KnowledgeSyncRequest request) {
            return null;
        }

        @Override
        public KnowledgeSyncResult deleteKnowledgeItem(KnowledgeItemDeleteRequest request) {
            return null;
        }

        @Override
        public KnowledgeChatResult chat(KnowledgeChatRequest request) {
            return null;
        }

        @Override
        public KnowledgeChatResult chatStream(KnowledgeChatRequest request, KnowledgeChatStreamHandler streamHandler) {
            return null;
        }
    }
}
