package com.thundax.kuzhambu.storage.infra.object.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.oss.client.ObjectStorageClient;
import com.thundax.kuzhambu.common.oss.model.ObjectStorageWriteResult;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class StoredObjectContentRepositoryImplTest {

    @Test
    void deleteShouldRemoveObjectByStoredObjectKey() throws Exception {
        RecordingObjectStorageClient client = new RecordingObjectStorageClient();
        StoredObjectContentRepositoryImpl store = new StoredObjectContentRepositoryImpl(client, "bucket", "/content/");
        StoredObject storage = new StoredObject();
        storage.setObjectKey("202601/source.pdf");

        store.delete(storage);

        assertEquals("202601/source.pdf", client.deletedKey);
    }

    @Test
    void deleteShouldExposeClientFailure() {
        RecordingObjectStorageClient client = new RecordingObjectStorageClient();
        client.deleteFailure = new IOException("delete failed");
        StoredObjectContentRepositoryImpl store = new StoredObjectContentRepositoryImpl(client, "bucket", "/content/");
        StoredObject storage = new StoredObject();
        storage.setObjectKey("202601/source.pdf");

        assertThrows(IOException.class, () -> store.delete(storage));
    }

    private static final class RecordingObjectStorageClient implements ObjectStorageClient {
        private String deletedKey;
        private IOException deleteFailure;

        @Override
        public ObjectStorageWriteResult put(String key, InputStream inputStream) {
            ObjectStorageWriteResult result = new ObjectStorageWriteResult();
            result.setKey(key);
            result.setSize(0L);
            return result;
        }

        @Override
        public InputStream get(String key) {
            return InputStream.nullInputStream();
        }

        @Override
        public boolean exists(String key) {
            return true;
        }

        @Override
        public void delete(String key) throws IOException {
            if (deleteFailure != null) {
                throw deleteFailure;
            }
            deletedKey = key;
        }
    }
}
