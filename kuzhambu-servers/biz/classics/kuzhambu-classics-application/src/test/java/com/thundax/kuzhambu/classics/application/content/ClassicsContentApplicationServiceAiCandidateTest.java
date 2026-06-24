package com.thundax.kuzhambu.classics.application.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateApplyCheck;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateDomainService;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.result.AiCandidateApplyContentResult;
import com.thundax.kuzhambu.classics.application.content.service.impl.ClassicsContentApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsTagBindingSupport;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsContentApplicationServiceAiCandidateTest {

    @Test
    void applyAiCandidateSummaryShouldUpdateSancaiAndGenerateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        entry.setSummary("old summary");
        entry.setContentUpdatedAt(new Date(1L));
        repository.sancaiEntryForAiApply = entry;

        AiCandidateDomainService aiCandidateDomainService = mockAiCandidateDomainService(
                check -> {
                    assertEquals(11L, check.getCandidateId());
                    assertEquals("SANCAI_ENTRY", check.getContentType());
                    assertEquals(11L, check.getContentId());
                    assertEquals("summary", check.getCapability());
                    return pendingCandidate();
                },
                (candidateId, resultFormat, resultPayload, markAppliedAt) -> {
                    assertEquals(11L, candidateId);
                    assertEquals("TEXT", resultFormat);
                    assertEquals("new summary", resultPayload);
                    assertEquals(Instant.class, markAppliedAt.getClass());
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service =
                serviceWithAiDomainService(repository, aiCandidateDomainService);
        AiCandidateApplyContentCommand command =
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, "summary", "new summary");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(1L, result.getVersionId());
        assertEquals(1, result.getVersionNo());
        assertEquals("new summary", entry.getSummary());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：摘要", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.insertVersionCount);
        assertEquals(1, repository.updateSancaiEntryAiCount);
        verify(aiCandidateDomainService).markApplied(eq(11L), eq("TEXT"), eq("new summary"), any(Instant.class));
    }

    @Test
    void applyAiCandidateSummaryShouldUpdateWangqiAndGenerateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        WangqiDocument document = new WangqiDocument(
                WangqiDocumentId.of(22L),
                "title",
                "old summary",
                WangqiContentFormat.HTML,
                "content",
                new Date(),
                null,
                WangqiDocumentVisibility.PUBLIC);
        repository.wangqiDocumentForAiApply = document;

        AiCandidateDomainService aiCandidateDomainService = mockAiCandidateDomainService(
                check -> {
                    assertEquals(22L, check.getCandidateId());
                    assertEquals("WANGQI_DOCUMENT", check.getContentType());
                    assertEquals(22L, check.getContentId());
                    assertEquals("summary", check.getCapability());
                    return pendingCandidate();
                },
                (candidateId, resultFormat, resultPayload, markAppliedAt) -> {
                    assertEquals(22L, candidateId);
                    assertEquals("TEXT", resultFormat);
                    assertEquals("new summary", resultPayload);
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service =
                serviceWithAiDomainService(repository, aiCandidateDomainService);
        AiCandidateApplyContentCommand command =
                applyCommand(22L, ClassicsContentType.WANGQI_DOCUMENT, 22L, "summary", "new summary");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.WANGQI_DOCUMENT, result.getContentType());
        assertEquals(22L, result.getContentId());
        assertEquals(1L, result.getVersionId());
        assertEquals(1, result.getVersionNo());
        assertEquals("new summary", document.getSummary());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：摘要", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.insertVersionCount);
        assertEquals(1, repository.updateWangqiDocumentAiCount);
        verify(aiCandidateDomainService).markApplied(eq(22L), eq("TEXT"), eq("new summary"), any(Instant.class));
    }

    @Test
    void applyAiCandidateSummaryShouldUpdateMingCustomsAndGenerateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(MingCustomsEntryId.of(33L));
        entry.setSummary("old summary");
        repository.mingCustomsEntryForAiApply = entry;

        AiCandidateDomainService aiCandidateDomainService = mockAiCandidateDomainService(
                check -> {
                    assertEquals(33L, check.getCandidateId());
                    assertEquals("MING_CUSTOMS", check.getContentType());
                    assertEquals(33L, check.getContentId());
                    assertEquals("summary", check.getCapability());
                    return pendingCandidate();
                },
                (candidateId, resultFormat, resultPayload, markAppliedAt) -> {
                    assertEquals(33L, candidateId);
                    assertEquals("TEXT", resultFormat);
                    assertEquals("new summary", resultPayload);
                    return candidateApplied();
                });

        ClassicsContentApplicationServiceImpl service =
                serviceWithAiDomainService(repository, aiCandidateDomainService);
        AiCandidateApplyContentCommand command =
                applyCommand(33L, ClassicsContentType.MING_CUSTOMS, 33L, "summary", "new summary");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.MING_CUSTOMS, result.getContentType());
        assertEquals(33L, result.getContentId());
        assertEquals(1L, result.getVersionId());
        assertEquals(1, result.getVersionNo());
        assertEquals("new summary", entry.getSummary());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：摘要", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.insertVersionCount);
        assertEquals(1, repository.updateMingCustomsEntryAiCount);
        verify(aiCandidateDomainService).markApplied(eq(33L), eq("TEXT"), eq("new summary"), any(Instant.class));
    }

    @Test
    void applyAiCandidateTagsShouldOnlyReplaceAiTagsAndCreateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        repository.sancaiEntryForAiApply = entry;
        repository.tags.add(manualTag(1L, 11L, "manual-tag"));
        repository.tags.add(aiTag(2L, 11L, "old-ai-tag"));

        AiCandidateDomainService aiCandidateDomainService = mockAiCandidateDomainService(
                check -> {
                    assertEquals(11L, check.getCandidateId());
                    assertEquals("SANCAI_ENTRY", check.getContentType());
                    assertEquals(11L, check.getContentId());
                    assertEquals("tags", check.getCapability());
                    return pendingCandidate();
                },
                (candidateId, resultFormat, resultPayload, markAppliedAt) -> candidateApplied());

        ClassicsContentApplicationServiceImpl service =
                serviceWithAiDomainService(repository, aiCandidateDomainService);
        AiCandidateApplyContentCommand command = applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                "tags",
                "{\"tags\":[\"ai-one\",\"ai-two\",\"ai-one\",\"\"]}");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：标签", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.deleteAiTagsCount);
        assertEquals(2, repository.insertTagCount);
        assertEquals(1, repository.insertVersionCount);
        assertEquals(3, repository.tags.size());
        assertEquals(
                1,
                repository.tags.stream()
                        .filter(tag -> tag.getSource() == ClassicsContentSource.MANUAL)
                        .count());
        assertEquals(
                2,
                repository.tags.stream()
                        .filter(tag -> tag.getSource() == ClassicsContentSource.AI)
                        .count());
        verify(aiCandidateDomainService)
                .markApplied(
                        eq(11L),
                        eq("TEXT"),
                        eq("{\"tags\":[\"ai-one\",\"ai-two\",\"ai-one\",\"\"]}"),
                        any(Instant.class));
    }

    @Test
    void applyAiCandidateTagsShouldSyncKnowledgeRefsWhenBindingSupportPresent() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        repository.sancaiEntryForAiApply = entry;
        ClassicsContentTag oldAiTag = aiTag(2L, 11L, "old-ai-tag");
        repository.tags.add(manualTag(1L, 11L, "manual-tag"));
        repository.tags.add(oldAiTag);

        AiCandidateDomainService aiCandidateDomainService = mockAiCandidateDomainService(
                check -> pendingCandidate(),
                (candidateId, resultFormat, resultPayload, markAppliedAt) -> candidateApplied());
        ClassicsTagBindingSupport tagBindingSupport = org.mockito.Mockito.mock(ClassicsTagBindingSupport.class);
        when(tagBindingSupport.bindAiTag(any(ContentTagCommand.class), any())).thenAnswer(invocation -> {
            ContentTagCommand command = invocation.getArgument(0);
            return command.toEntity();
        });

        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, null, aiCandidateDomainService, tagBindingSupport, null);

        service.applyAiCandidate(
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, "tags", "{\"tags\":[\"ai-one\",\"ai-two\"]}"));

        verify(tagBindingSupport).removeTagRef(oldAiTag);
        verify(tagBindingSupport, times(2)).bindAiTag(any(ContentTagCommand.class), any());
        verify(tagBindingSupport, times(2)).syncTagRef(any(ClassicsContentTag.class));
    }

    @Test
    void applyAiCandidateQaShouldOnlyReplaceAiQaPairsAndCreateAiAppliedVersion() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        repository.sancaiEntryForAiApply = entry;
        repository.qaPairs.add(manualQaPair(1L, 11L, "manual-q", "manual-a"));
        repository.qaPairs.add(aiQaPair(2L, 11L, "old-q", "old-a"));

        AiCandidateDomainService aiCandidateDomainService = mockAiCandidateDomainService(
                check -> {
                    assertEquals(11L, check.getCandidateId());
                    assertEquals("SANCAI_ENTRY", check.getContentType());
                    assertEquals(11L, check.getContentId());
                    assertEquals("qa", check.getCapability());
                    return pendingCandidate();
                },
                (candidateId, resultFormat, resultPayload, markAppliedAt) -> candidateApplied());

        ClassicsContentApplicationServiceImpl service =
                serviceWithAiDomainService(repository, aiCandidateDomainService);
        AiCandidateApplyContentCommand command = applyCommand(
                11L,
                ClassicsContentType.SANCAI_ENTRY,
                11L,
                "qa",
                "{\"qaPairs\":[{\"question\":\"q1\",\"answer\":\"a\"},{\"question\":\"q2\",\"answer\":\"b\"},{\"question\":\"q1\",\"answer\":\"a\"}]}");

        AiCandidateApplyContentResult result = service.applyAiCandidate(command);

        assertEquals(ClassicsContentType.SANCAI_ENTRY, result.getContentType());
        assertEquals(11L, result.getContentId());
        assertEquals(ClassicsContentChangeType.AI_APPLIED, repository.lastInsertedVersion.getChangeType());
        assertEquals("AI 应用：问答对", repository.lastInsertedVersion.getChangeSummary());
        assertEquals(1, repository.deleteAiQaPairsCount);
        assertEquals(2, repository.insertQaPairCount);
        assertEquals(3, repository.qaPairs.size());
        assertEquals(
                1,
                repository.qaPairs.stream()
                        .filter(pair -> pair.getSource() == ClassicsContentSource.MANUAL)
                        .count());
        assertEquals(
                2,
                repository.qaPairs.stream()
                        .filter(pair -> pair.getSource() == ClassicsContentSource.AI)
                        .count());
        verify(aiCandidateDomainService)
                .markApplied(
                        eq(11L),
                        eq("TEXT"),
                        eq(
                                "{\"qaPairs\":[{\"question\":\"q1\",\"answer\":\"a\"},{\"question\":\"q2\",\"answer\":\"b\"},{\"question\":\"q1\",\"answer\":\"a\"}]}"),
                        any(Instant.class));
    }

    @Test
    void applyAiCandidateShouldFailWhenAiCandidateNotPending() {
        FakeRepository repository = new FakeRepository();
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(11L));
        entry.setSummary("old summary");
        repository.sancaiEntryForAiApply = entry;

        AiCandidateDomainService aiCandidateDomainService = mockAiCandidateDomainService(
                check -> {
                    throw new DomainException(
                            "AI-INVOCATION-409",
                            "ai.candidate.not-pending",
                            "AI candidate is not pending: " + check.getCandidateId());
                },
                (candidateId, resultFormat, resultPayload, markAppliedAt) -> {
                    throw new IllegalStateException("markApplied should not be called");
                });

        ClassicsContentApplicationServiceImpl service =
                serviceWithAiDomainService(repository, aiCandidateDomainService);
        AiCandidateApplyContentCommand command =
                applyCommand(11L, ClassicsContentType.SANCAI_ENTRY, 11L, "summary", "new summary");

        assertThrows(DomainException.class, () -> service.applyAiCandidate(command));

        assertEquals("old summary", entry.getSummary());
        assertEquals(0, repository.insertVersionCount);
        assertEquals(0, repository.updateSancaiEntryAiCount);
        verify(aiCandidateDomainService, never()).markApplied(anyLong(), any(), any(), any(Instant.class));
    }

    private static ClassicsContentApplicationServiceImpl serviceWithAiDomainService(
            ClassicsContentRepository repository, AiCandidateDomainService aiCandidateDomainService) {
        return new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, null, aiCandidateDomainService, null, null);
    }

    private static AiCandidateApplyContentCommand applyCommand(
            Long candidateId, ClassicsContentType contentType, Long contentId, String capability, String payload) {
        AiCandidateApplyContentCommand command = new AiCandidateApplyContentCommand();
        command.setCandidateId(candidateId);
        command.setContentType(contentType);
        command.setContentId(contentId);
        command.setCapability(capability);
        command.setResultFormat("TEXT");
        command.setResultPayload(payload);
        return command;
    }

    private static AiCandidate pendingCandidate() {
        AiCandidate candidate = new AiCandidate();
        candidate.setCandidateId(11L);
        candidate.setStatus("PENDING");
        return candidate;
    }

    private static AiCandidate candidateApplied() {
        AiCandidate candidate = new AiCandidate();
        candidate.setCandidateId(11L);
        candidate.setStatus("APPLIED");
        return candidate;
    }

    private static AiCandidateDomainService mockAiCandidateDomainService(
            java.util.function.Function<AiCandidateApplyCheck, AiCandidate> requirePending, AiMarkApplied markApplied) {
        AiCandidateDomainService aiCandidateDomainService = org.mockito.Mockito.mock(AiCandidateDomainService.class);
        when(aiCandidateDomainService.requirePendingForApply(any(AiCandidateApplyCheck.class)))
                .thenAnswer(invocation -> {
                    AiCandidateApplyCheck check = invocation.getArgument(0);
                    return requirePending.apply(check);
                });
        when(aiCandidateDomainService.markApplied(anyLong(), any(), any(), any(Instant.class)))
                .thenAnswer(invocation -> {
                    Long candidateId = invocation.getArgument(0);
                    String resultFormat = invocation.getArgument(1);
                    String resultPayload = invocation.getArgument(2);
                    Instant markAppliedAt = invocation.getArgument(3);
                    return markApplied.apply(candidateId, resultFormat, resultPayload, markAppliedAt);
                });
        return aiCandidateDomainService;
    }

    @FunctionalInterface
    private interface AiMarkApplied {
        AiCandidate apply(Long candidateId, String resultFormat, String resultPayload, Instant markAppliedAt);
    }

    private static ClassicsContentTag manualTag(Long id, Long contentId, String tagName) {
        ClassicsContentTag tag = new ClassicsContentTag();
        tag.setId(ClassicsContentTagId.of(id));
        tag.setContentType(ClassicsContentType.SANCAI_ENTRY);
        tag.setContentId(ClassicsContentId.of(contentId));
        tag.setTagNameSnapshot(tagName);
        tag.setSource(ClassicsContentSource.MANUAL);
        tag.setStatus(ClassicsContentTagStatus.ACTIVE);
        return tag;
    }

    private static ClassicsContentTag aiTag(Long id, Long contentId, String tagName) {
        ClassicsContentTag tag = manualTag(id, contentId, tagName);
        tag.setSource(ClassicsContentSource.AI);
        return tag;
    }

    private static ClassicsContentQaPair manualQaPair(Long id, Long contentId, String question, String answer) {
        ClassicsContentQaPair qaPair = new ClassicsContentQaPair();
        qaPair.setId(ClassicsContentQaPairId.of(id));
        qaPair.setContentType(ClassicsContentType.SANCAI_ENTRY);
        qaPair.setContentId(ClassicsContentId.of(contentId));
        qaPair.setQuestion(question);
        qaPair.setAnswer(answer);
        qaPair.setSource(ClassicsContentSource.MANUAL);
        return qaPair;
    }

    private static ClassicsContentQaPair aiQaPair(Long id, Long contentId, String question, String answer) {
        ClassicsContentQaPair qaPair = manualQaPair(id, contentId, question, answer);
        qaPair.setSource(ClassicsContentSource.AI);
        return qaPair;
    }

    private static final class FakeRepository implements ClassicsContentRepository {
        private final List<ClassicsContentVersion> versions = new ArrayList<>();
        private final List<ClassicsContentTag> tags = new ArrayList<>();
        private final List<ClassicsContentQaPair> qaPairs = new ArrayList<>();
        private ClassicsContentVersion lastInsertedVersion;
        private int insertVersionCount;
        private int updateSancaiEntryAiCount;
        private int updateWangqiDocumentAiCount;
        private int updateMingCustomsEntryAiCount;
        private int deleteAiTagsCount;
        private int deleteAiQaPairsCount;
        private int insertTagCount;
        private int insertQaPairCount;
        private SancaiEntry sancaiEntryForAiApply;
        private WangqiDocument wangqiDocumentForAiApply;
        private MingCustomsEntry mingCustomsEntryForAiApply;

        @Override
        public List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId) {
            return versions;
        }

        @Override
        public ClassicsContentVersionId insertVersion(ClassicsContentVersion version) {
            ClassicsContentVersionId id = ClassicsContentVersionId.of(versions.size() + 1L);
            version.setId(id);
            version.setVersionNo(versions.size() + 1);
            versions.add(version);
            insertVersionCount++;
            lastInsertedVersion = version;
            return id;
        }

        @Override
        public List<ClassicsContentTag> listTags(
                String contentType,
                ClassicsContentId contentId,
                com.thundax.kuzhambu.common.core.sort.SortDirection sortDirection) {
            return tags;
        }

        @Override
        public int maxTagPriority(String contentType, ClassicsContentId contentId) {
            return tags.size();
        }

        @Override
        public ClassicsContentTagId insertTag(ClassicsContentTag tag) {
            tags.add(tag);
            insertTagCount++;
            return ClassicsContentTagId.of((long) tags.size());
        }

        @Override
        public ClassicsContentTag getTagById(ClassicsContentTagId id) {
            return null;
        }

        @Override
        public int updateTagPriority(ClassicsContentTag tag) {
            return 1;
        }

        @Override
        public int updateTag(ClassicsContentTag tag) {
            return 1;
        }

        @Override
        public int deleteTagById(String contentType, ClassicsContentId contentId, ClassicsContentTagId id) {
            return 1;
        }

        @Override
        public List<ClassicsContentQaPair> listQaPairs(
                String contentType,
                ClassicsContentId contentId,
                com.thundax.kuzhambu.common.core.sort.SortDirection sortDirection) {
            return qaPairs;
        }

        @Override
        public List<ClassicsContentQaPair> listQaPairs(
                com.thundax.kuzhambu.common.core.sort.SortDirection sortDirection) {
            return qaPairs;
        }

        @Override
        public int maxQaPairPriority() {
            return qaPairs.size();
        }

        @Override
        public ClassicsContentQaPairId insertQaPair(ClassicsContentQaPair qaPair) {
            qaPairs.add(qaPair);
            insertQaPairCount++;
            return ClassicsContentQaPairId.of((long) qaPairs.size());
        }

        @Override
        public ClassicsContentQaPair getQaPairById(ClassicsContentQaPairId id) {
            return null;
        }

        @Override
        public int updateQaPairPriority(ClassicsContentQaPair qaPair) {
            return 1;
        }

        @Override
        public int updateQaPair(ClassicsContentQaPair qaPair) {
            return 1;
        }

        @Override
        public int deleteQaPairById(ClassicsContentQaPairId id) {
            return 1;
        }

        @Override
        public ClassicsContentVersion getVersionById(ClassicsContentVersionId id) {
            return versions.stream()
                    .filter(version -> version.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public int deleteVersions(String contentType, ClassicsContentId contentId) {
            return 1;
        }

        @Override
        public SancaiEntry getSancaiEntryForAiApply(ClassicsContentId contentId) {
            return sancaiEntryForAiApply;
        }

        @Override
        public int updateSancaiEntryAiFields(SancaiEntry entry) {
            updateSancaiEntryAiCount++;
            return 1;
        }

        @Override
        public WangqiDocument getWangqiDocumentForAiApply(ClassicsContentId contentId) {
            return wangqiDocumentForAiApply;
        }

        @Override
        public int updateWangqiDocumentAiFields(WangqiDocument document) {
            updateWangqiDocumentAiCount++;
            return 1;
        }

        @Override
        public MingCustomsEntry getMingCustomsEntryForAiApply(ClassicsContentId contentId) {
            return mingCustomsEntryForAiApply;
        }

        @Override
        public int updateMingCustomsEntryAiFields(MingCustomsEntry entry) {
            updateMingCustomsEntryAiCount++;
            return 1;
        }

        @Override
        public int deleteAiTags(String contentType, ClassicsContentId contentId) {
            deleteAiTagsCount++;
            tags.removeIf(tag -> tag.getContentType() != null
                    && tag.getContentType().value().equals(contentType)
                    && tag.getContentId() != null
                    && tag.getContentId().equals(contentId)
                    && tag.getSource() == ClassicsContentSource.AI);
            return 1;
        }

        @Override
        public int deleteAiQaPairs(String contentType, ClassicsContentId contentId) {
            deleteAiQaPairsCount++;
            qaPairs.removeIf(pair -> pair.getContentType() != null
                    && pair.getContentType().value().equals(contentType)
                    && pair.getContentId() != null
                    && pair.getContentId().equals(contentId)
                    && pair.getSource() == ClassicsContentSource.AI);
            return 1;
        }

        @Override
        public ClassicsContentExportJobId insertExportJob(ClassicsContentExportJob exportJob) {
            return null;
        }

        @Override
        public ClassicsContentExportJob getExportJobById(ClassicsContentExportJobId id) {
            return null;
        }

        @Override
        public int updateExportJob(ClassicsContentExportJob exportJob) {
            return 1;
        }

        @Override
        public int markExportJobCompleted(
                ClassicsContentExportJobId id,
                com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId storageObjectId,
                Date expiresAt,
                int itemCount,
                int assetCount) {
            return 1;
        }

        @Override
        public int markExportJobFailed(ClassicsContentExportJobId id) {
            return 1;
        }

        @Override
        public int markExportJobExpired(ClassicsContentExportJobId id) {
            return 1;
        }

        @Override
        public Page<ClassicsContentExportJob> pageExportJobs(
                String contentType, String exportKind, String status, int pageNo, int pageSize) {
            return new Page<>();
        }
    }
}
