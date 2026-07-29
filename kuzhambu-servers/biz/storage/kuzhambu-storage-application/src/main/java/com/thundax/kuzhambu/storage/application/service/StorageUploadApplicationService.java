package com.thundax.kuzhambu.storage.application.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.storage.application.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;

public interface StorageUploadApplicationService {

    @LayerPublicApi(reason = "业务对象上传存储对象内容并落元数据的跨模块入口")
    StoredObject upload(UploadStorageObjectCommand command);
}
