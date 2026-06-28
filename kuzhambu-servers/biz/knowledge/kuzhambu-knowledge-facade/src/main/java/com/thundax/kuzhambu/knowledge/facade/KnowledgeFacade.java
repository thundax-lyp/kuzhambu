package com.thundax.kuzhambu.knowledge.facade;

import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeSummaryFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSummaryFacadeResponse;

public interface KnowledgeFacade {

    KnowledgeSummaryFacadeResponse summary(KnowledgeSummaryFacadeRequest request);
}
