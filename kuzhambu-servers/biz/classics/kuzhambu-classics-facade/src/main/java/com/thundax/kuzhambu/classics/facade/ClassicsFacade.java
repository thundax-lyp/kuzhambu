package com.thundax.kuzhambu.classics.facade;

import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;

public interface ClassicsFacade {

    ClassicsSummaryFacadeResponse summary(ClassicsSummaryFacadeRequest request);
}
