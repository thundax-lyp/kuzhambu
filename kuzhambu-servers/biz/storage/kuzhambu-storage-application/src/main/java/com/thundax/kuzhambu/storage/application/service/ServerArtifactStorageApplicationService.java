package com.thundax.kuzhambu.storage.application.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.storage.application.service.command.UploadServerArtifactCommand;
import com.thundax.kuzhambu.storage.application.service.result.ServerArtifactStoredResult;

public interface ServerArtifactStorageApplicationService {

    @LayerPublicApi(reason = "业务域生成服务端临时产物后写入存储对象的跨模块入口")
    ServerArtifactStoredResult storeServerArtifact(UploadServerArtifactCommand command);
}
