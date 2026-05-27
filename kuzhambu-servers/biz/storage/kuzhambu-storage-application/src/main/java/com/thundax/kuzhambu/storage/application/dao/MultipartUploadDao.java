package com.thundax.kuzhambu.storage.application.dao;

import com.thundax.kuzhambu.storage.application.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.application.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.valueobject.MultipartUploadPartId;
import com.thundax.kuzhambu.storage.domain.object.valueobject.MultipartUploadSessionId;
import java.util.List;

public interface MultipartUploadDao {

    MultipartUploadSessionId insertMultipartSession(MultipartUploadSession session);

    MultipartUploadSession getMultipartSessionByUploadId(String uploadId);

    int updateMultipartSession(MultipartUploadSession session);

    MultipartUploadPartId insertMultipartPart(MultipartUploadPart part);

    MultipartUploadPart getMultipartPart(String uploadId, Integer partNumber);

    List<MultipartUploadPart> listMultipartParts(String uploadId);

    int countMultipartParts(String uploadId);
}
