package com.thundax.kuzhambu.biz.storage.dao;

import com.thundax.kuzhambu.biz.storage.entity.MultipartUploadPart;
import com.thundax.kuzhambu.biz.storage.entity.MultipartUploadSession;
import com.thundax.kuzhambu.biz.storage.entity.valueobject.MultipartUploadPartId;
import com.thundax.kuzhambu.biz.storage.entity.valueobject.MultipartUploadSessionId;
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
