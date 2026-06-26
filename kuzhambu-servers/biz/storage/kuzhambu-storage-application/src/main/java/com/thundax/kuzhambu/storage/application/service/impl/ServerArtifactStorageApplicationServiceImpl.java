package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadResult;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadStreamHelper;
import com.thundax.kuzhambu.storage.application.service.ServerArtifactStorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.UploadServerArtifactCommand;
import com.thundax.kuzhambu.storage.application.service.result.ServerArtifactStoredResult;
import java.io.ByteArrayInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@BizExceptionBoundary
public class ServerArtifactStorageApplicationServiceImpl implements ServerArtifactStorageApplicationService {

    private final StorageUploadStreamHelper storageUploadStreamHelper;

    public ServerArtifactStorageApplicationServiceImpl(StorageUploadStreamHelper storageUploadStreamHelper) {
        this.storageUploadStreamHelper = storageUploadStreamHelper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServerArtifactStoredResult storeServerArtifact(UploadServerArtifactCommand command) {
        validateCommand(command);
        StorageUploadResult uploadResult = storageUploadStreamHelper.uploadServerArtifact(
                new ByteArrayInputStream(command.getContentBytes()),
                command.getOriginalFilename(),
                command.getContentType(),
                command.getSizeBytes() == null ? command.getContentBytes().length : command.getSizeBytes());
        if (uploadResult == null || uploadResult.hasError()) {
            throw new BizException("服务端产物入库存储失败: " + (uploadResult == null ? "unknown" : uploadResult.getError()));
        }
        if (uploadResult.getStorage() == null || uploadResult.getStorage().getId() == null) {
            throw new BizException("服务端产物入库存储失败: 存储对象为空");
        }
        return new ServerArtifactStoredResult(
                uploadResult.getStorage().getId(),
                uploadResult.getStorage().getOriginalFilename(),
                uploadResult.getStorage().getContentType(),
                uploadResult.getStorage().getSize());
    }

    private void validateCommand(UploadServerArtifactCommand command) {
        if (command == null || command.getContentBytes() == null || command.getContentBytes().length == 0) {
            throw new BizException("服务端产物内容不能为空");
        }
        if (StringUtils.isBlank(command.getOriginalFilename())) {
            throw new BizException("服务端产物文件名不能为空");
        }
        if (StringUtils.isBlank(command.getContentType())) {
            throw new BizException("服务端产物内容类型不能为空");
        }
    }
}
