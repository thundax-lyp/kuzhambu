package com.thundax.kuzhambu.classics.application.publication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.publication.query.ClassicsPublicationJobPageQuery;
import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationCreationApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsPublicationApplicationServiceTest {
    @Test
    void shouldForwardEveryPageFilterToRepository() {
        ClassicsPublicationJobRepository repository = mock(ClassicsPublicationJobRepository.class);
        ClassicsPublicationApplicationServiceImpl service = new ClassicsPublicationApplicationServiceImpl(
                mock(ClassicsPublicationCreationApplicationServiceImpl.class), repository);
        ClassicsPublicationJobPageQuery query = new ClassicsPublicationJobPageQuery(
                ClassicsPublicationJobType.PUBLISH,
                ClassicsPublicationJobResultStatus.RUNNING,
                ClassicsPublicationJobStatus.ES_PREPARED,
                ClassicsContentType.WANGQI_DOCUMENT,
                "王圻");
        when(repository.page(
                        ClassicsPublicationJobType.PUBLISH,
                        ClassicsPublicationJobResultStatus.RUNNING,
                        ClassicsPublicationJobStatus.ES_PREPARED,
                        ClassicsContentType.WANGQI_DOCUMENT,
                        "王圻",
                        3,
                        17))
                .thenReturn(PageResult.of(3, 17, 0, List.<ClassicsPublicationJob>of()));

        service.page(query, new PageQuery(3, 17));

        verify(repository)
                .page(
                        ClassicsPublicationJobType.PUBLISH,
                        ClassicsPublicationJobResultStatus.RUNNING,
                        ClassicsPublicationJobStatus.ES_PREPARED,
                        ClassicsContentType.WANGQI_DOCUMENT,
                        "王圻",
                        3,
                        17);
    }
}
