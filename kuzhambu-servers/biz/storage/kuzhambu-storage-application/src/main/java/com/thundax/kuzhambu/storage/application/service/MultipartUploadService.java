package com.thundax.kuzhambu.storage.application.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.storage.application.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.application.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.application.entity.StoredObject;
import com.thundax.kuzhambu.storage.application.service.command.AbortMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.service.command.CompleteMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.service.command.InitMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.service.command.UploadMultipartPartCommand;

public interface MultipartUploadService {

    @LayerPublicApi(reason = "分片上传流程初始化会话的业务入口")
    MultipartUploadSession init(InitMultipartUploadCommand command);

    @LayerPublicApi(reason = "分片上传流程写入单个分片的业务入口")
    MultipartUploadPart uploadPart(UploadMultipartPartCommand command);

    @LayerPublicApi(reason = "分片上传流程合并并生成存储对象的业务入口")
    StoredObject complete(CompleteMultipartUploadCommand command);

    @LayerPublicApi(reason = "分片上传流程取消会话的业务入口")
    int abort(AbortMultipartUploadCommand command);
}
