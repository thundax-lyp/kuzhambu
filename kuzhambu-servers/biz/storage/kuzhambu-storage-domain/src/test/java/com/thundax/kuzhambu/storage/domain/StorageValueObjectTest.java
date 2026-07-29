package com.thundax.kuzhambu.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.storage.domain.object.codec.MultipartPartNumberCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.MultipartPartSizeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageBucketNameCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageByteSizeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageMimeTypeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageObjectKeyCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageOwnerParamsCodec;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartNumber;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageBucketName;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageObjectKey;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerParams;
import org.junit.jupiter.api.Test;

class StorageValueObjectTest {

    @Test
    void stringValueObjectsShouldTrimAndRejectBlankValues() {
        assertEquals("image/png", new StorageMimeType(" image/png ").value());
        assertEquals("storage-bucket", new StorageBucketName(" storage-bucket ").value());
        assertEquals("object/path.png", new StorageObjectKey(" object/path.png ").value());

        assertThrows(IllegalArgumentException.class, () -> new StorageMimeType(" "));
        assertThrows(IllegalArgumentException.class, () -> new StorageBucketName(" "));
        assertThrows(IllegalArgumentException.class, () -> new StorageObjectKey(" "));
        assertThrows(NullPointerException.class, () -> new StorageMimeType(null));
        assertThrows(NullPointerException.class, () -> new StorageBucketName(null));
        assertThrows(NullPointerException.class, () -> new StorageObjectKey(null));
    }

    @Test
    void storageByteSizeShouldRejectNullAndNegativeValues() {
        assertEquals(0L, new StorageByteSize(0L).value());
        assertEquals(1024L, new StorageByteSize(1024L).value());

        assertThrows(NullPointerException.class, () -> new StorageByteSize(null));
        assertThrows(IllegalArgumentException.class, () -> new StorageByteSize(-1L));
    }

    @Test
    void multipartPartNumberShouldRejectNullAndValuesBelowOne() {
        assertEquals(1, new MultipartPartNumber(1).value());
        assertEquals(2, new MultipartPartNumber(2).value());

        assertThrows(NullPointerException.class, () -> new MultipartPartNumber(null));
        assertThrows(IllegalArgumentException.class, () -> new MultipartPartNumber(0));
    }

    @Test
    void multipartPartSizeShouldRejectNullZeroAndNegativeValues() {
        assertEquals(1L, new MultipartPartSize(1L).value());
        assertEquals(5_242_880L, new MultipartPartSize(5_242_880L).value());

        assertThrows(NullPointerException.class, () -> new MultipartPartSize(null));
        assertThrows(IllegalArgumentException.class, () -> new MultipartPartSize(0L));
        assertThrows(IllegalArgumentException.class, () -> new MultipartPartSize(-1L));
    }

    @Test
    void storageOwnerParamsShouldTrimOuterWhitespaceOnly() {
        StorageOwnerParams params = new StorageOwnerParams(" {\"name\":\" value \"} ");

        assertEquals("{\"name\":\" value \"}", params.value());
        assertThrows(NullPointerException.class, () -> new StorageOwnerParams(null));
    }

    @Test
    void stringCodecsShouldOwnNullableBoundaryConversion() {
        assertNull(StorageMimeTypeCodec.toDomain(null));
        assertNull(StorageMimeTypeCodec.toDomain(" "));
        assertNull(StorageBucketNameCodec.toDomain(null));
        assertNull(StorageBucketNameCodec.toDomain(" "));
        assertNull(StorageObjectKeyCodec.toDomain(null));
        assertNull(StorageObjectKeyCodec.toDomain(" "));

        StorageMimeType mimeType = StorageMimeTypeCodec.toDomain(" image/jpeg ");
        StorageBucketName bucketName = StorageBucketNameCodec.toDomain(" storage ");
        StorageObjectKey objectKey = StorageObjectKeyCodec.toDomain(" path/image.jpg ");

        assertEquals("image/jpeg", StorageMimeTypeCodec.toValue(mimeType));
        assertEquals("storage", StorageBucketNameCodec.toValue(bucketName));
        assertEquals("path/image.jpg", StorageObjectKeyCodec.toValue(objectKey));
        assertNull(StorageMimeTypeCodec.toValue(null));
        assertNull(StorageBucketNameCodec.toValue(null));
        assertNull(StorageObjectKeyCodec.toValue(null));
    }

    @Test
    void sizeAndPartCodecsShouldOwnNullableBoundaryConversion() {
        assertNull(StorageByteSizeCodec.toDomain(null));
        assertNull(MultipartPartSizeCodec.toDomain(null));
        assertNull(MultipartPartNumberCodec.toDomain(null));

        StorageByteSize size = StorageByteSizeCodec.toDomain(0L);
        MultipartPartSize partSize = MultipartPartSizeCodec.toDomain(5_242_880L);
        MultipartPartNumber partNumber = MultipartPartNumberCodec.toDomain(3);

        assertEquals(0L, StorageByteSizeCodec.toValue(size));
        assertEquals(5_242_880L, MultipartPartSizeCodec.toValue(partSize));
        assertEquals(3, MultipartPartNumberCodec.toValue(partNumber));
        assertNull(StorageByteSizeCodec.toValue(null));
        assertNull(MultipartPartSizeCodec.toValue(null));
        assertNull(MultipartPartNumberCodec.toValue(null));
    }

    @Test
    void ownerParamsCodecShouldOwnNullableBoundaryConversion() {
        assertNull(StorageOwnerParamsCodec.toDomain(null));

        StorageOwnerParams ownerParams = StorageOwnerParamsCodec.toDomain(" {\"source\":\"manual\"} ");

        assertEquals("{\"source\":\"manual\"}", StorageOwnerParamsCodec.toValue(ownerParams));
        assertNull(StorageOwnerParamsCodec.toValue(null));
    }
}
