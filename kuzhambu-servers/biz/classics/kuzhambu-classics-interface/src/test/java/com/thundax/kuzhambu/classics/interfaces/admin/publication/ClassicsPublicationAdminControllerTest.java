package com.thundax.kuzhambu.classics.interfaces.admin.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationJobView;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.ClassicsPublicationAdminController;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationJobGetRequest;
import org.junit.jupiter.api.Test;

class ClassicsPublicationAdminControllerTest {
    @Test
    void shouldReturnReadOnlyJobDetailWithoutPayloadContent() {
        ClassicsPublicationApplicationService service = mock(ClassicsPublicationApplicationService.class);
        ClassicsPublicationJobId jobId = new ClassicsPublicationJobId(101L);
        ClassicsPublicationJob job = new ClassicsPublicationJob();
        job.setId(jobId);
        job.setJobType(ClassicsPublicationJobType.PUBLISH);
        job.setJobStatus(ClassicsPublicationJobStatus.ES_PREPARED);
        job.setJobResultStatus(ClassicsPublicationJobResultStatus.FAILED);
        job.setContentType(ClassicsContentType.SANCAI_ENTRY);
        job.setContentId(201L);
        job.setContentTitleSnapshot("测试稿件");
        job.setFailureReason("provider timeout");
        job.setDetailJson("{\"provider\":\"FASTGPT\",\"timeout\":true}");
        when(service.get(jobId))
                .thenReturn(new ClassicsPublicationJobView(job, ClassicsPublicationJobStatus.FASTGPT_PREPARED));

        var response = new ClassicsPublicationAdminController(service).get(new ClassicsPublicationJobGetRequest(101L));

        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.failureStep()).isEqualTo("FASTGPT_PREPARED");
        assertThat(response.contentTitleSnapshot()).isEqualTo("测试稿件");
        assertThat(response.detailJsonSummary()).doesNotContain("snapshot", "apiKey");
    }
}
