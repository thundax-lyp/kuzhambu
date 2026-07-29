package com.thundax.kuzhambu.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartNumber;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageBucketName;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageObjectKey;
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
}
