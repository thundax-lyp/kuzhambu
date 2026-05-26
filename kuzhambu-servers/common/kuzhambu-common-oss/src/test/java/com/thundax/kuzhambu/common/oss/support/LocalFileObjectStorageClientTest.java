package com.thundax.kuzhambu.common.oss.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.oss.model.ObjectStorageWriteResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LocalFileObjectStorageClientTest {

    @TempDir
    private Path temporaryFolder;

    @Test
    public void shouldWriteReadAndDeleteLocalObject() throws Exception {
        LocalFileObjectStorageClient client = new LocalFileObjectStorageClient(temporaryFolder.toString(), "file:");

        ObjectStorageWriteResult result =
                client.put("avatars/user.txt", new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));

        assertEquals("avatars/user.txt", result.getKey());
        assertEquals("file:avatars/user.txt", result.getLocation());
        assertEquals(5L, result.getSize());
        assertTrue(client.exists("avatars/user.txt"));
        assertEquals("hello", IOUtils.toString(client.get("avatars/user.txt"), StandardCharsets.UTF_8));

        client.delete("avatars/user.txt");

        assertFalse(client.exists("avatars/user.txt"));
    }

    @Test
    public void shouldRejectEscapedKey() throws Exception {
        LocalFileObjectStorageClient client = new LocalFileObjectStorageClient(temporaryFolder.toString(), "file:");

        assertThrows(
                IllegalArgumentException.class,
                () -> client.put("../outside.txt", new ByteArrayInputStream("bad".getBytes(StandardCharsets.UTF_8))));
    }
}
