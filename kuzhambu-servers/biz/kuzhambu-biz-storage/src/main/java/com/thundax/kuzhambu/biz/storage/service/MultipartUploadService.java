package com.thundax.kuzhambu.biz.storage.service;

import com.thundax.kuzhambu.biz.storage.entity.MultipartUploadPart;
import com.thundax.kuzhambu.biz.storage.entity.MultipartUploadSession;
import com.thundax.kuzhambu.biz.storage.entity.StoredObject;
import com.thundax.kuzhambu.biz.storage.service.command.AbortMultipartUploadCommand;
import com.thundax.kuzhambu.biz.storage.service.command.CompleteMultipartUploadCommand;
import com.thundax.kuzhambu.biz.storage.service.command.InitMultipartUploadCommand;
import com.thundax.kuzhambu.biz.storage.service.command.UploadMultipartPartCommand;
import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;

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
