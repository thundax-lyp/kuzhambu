package com.thundax.kuzhambu.storage.application.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.storage.application.query.GetReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.query.OpenReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.result.StoredObjectContentResult;

public interface StorageContentApplicationService {

    @LayerPublicApi(reason = "存储对象下载或预览前校验内容可读性的业务入口")
    boolean existsReadableContent(GetReadableStorageContentQuery query);

    @LayerPublicApi(reason = "存储对象内容读取的业务入口")
    StoredObjectContentResult openReadableContent(OpenReadableStorageContentQuery query);
}
