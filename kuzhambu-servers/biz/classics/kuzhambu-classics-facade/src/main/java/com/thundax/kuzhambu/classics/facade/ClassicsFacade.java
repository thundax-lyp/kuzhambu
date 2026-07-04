package com.thundax.kuzhambu.classics.facade;

import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;

public interface ClassicsFacade {

    ClassicsSummaryFacadeResponse summary(ClassicsSummaryFacadeRequest request);

    ClassicsPublicContentsFacadeResponse listPublicContents();

    ClassicsPublicContentFacadeResponse getPublicContent(ClassicsPublicContentFacadeRequest request);

    ClassicsQaKnowledgeFacadeResponse getQaKnowledge(ClassicsQaKnowledgeFacadeRequest request);
}
