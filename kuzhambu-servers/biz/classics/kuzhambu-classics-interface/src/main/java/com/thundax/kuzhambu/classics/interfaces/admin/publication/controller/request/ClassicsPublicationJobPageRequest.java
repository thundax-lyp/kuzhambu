package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassicsPublicationJobPageRequest extends PageRequest {
    private ClassicsPublicationJobType jobType;
    private ClassicsPublicationJobResultStatus jobResultStatus;
    private ClassicsPublicationJobStatus jobStatus;
    private ClassicsContentType contentType;
    private String keyword;
}
