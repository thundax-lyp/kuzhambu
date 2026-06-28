package com.thundax.kuzhambu.storage.facade;

import com.thundax.kuzhambu.storage.facade.request.GetReadableContentFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.GetReadableContentFacadeResponse;

public interface StorageReadableContentFacade {

    boolean existsReadableContent(GetReadableContentFacadeRequest request);

    GetReadableContentFacadeResponse getReadableContent(GetReadableContentFacadeRequest request);
}
