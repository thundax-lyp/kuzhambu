package com.thundax.kuzhambu.common.oss.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.thundax.kuzhambu.common.oss.model.ObjectStorageWriteResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class S3ObjectStorageClientTest {

    @Test
    public void shouldWriteS3Object() throws Exception {
        AmazonS3 amazonS3 = s3();
        S3ObjectStorageClient client = new S3ObjectStorageClient(amazonS3, "bucket-a", "s3://bucket-a/");

        ObjectStorageWriteResult result =
                client.put("avatars/user.txt", new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));

        S3Stub stub = stub(amazonS3);
        assertEquals("bucket-a", stub.bucket);
        assertEquals("avatars/user.txt", stub.key);
        assertEquals(5L, stub.metadata.getContentLength());
        assertEquals("avatars/user.txt", result.getKey());
        assertEquals("s3://bucket-a/avatars/user.txt", result.getLocation());
        assertEquals(5L, result.getSize());
    }

    @Test
    public void shouldDelegateExistsAndDelete() {
        AmazonS3 amazonS3 = s3();
        stub(amazonS3).exists = true;
        S3ObjectStorageClient client = new S3ObjectStorageClient(amazonS3, "bucket-a", "s3://bucket-a/");

        assertTrue(client.exists("a.txt"));
        client.delete("a.txt");

        assertEquals("bucket-a", stub(amazonS3).deletedBucket);
        assertEquals("a.txt", stub(amazonS3).deletedKey);
    }

    private AmazonS3 s3() {
        return (AmazonS3)
                Proxy.newProxyInstance(AmazonS3.class.getClassLoader(), new Class<?>[] {AmazonS3.class}, new S3Stub());
    }

    private S3Stub stub(AmazonS3 amazonS3) {
        return (S3Stub) Proxy.getInvocationHandler(amazonS3);
    }

    private static class S3Stub implements InvocationHandler {

        private boolean exists;
        private String bucket;
        private String key;
        private ObjectMetadata metadata;
        private String deletedBucket;
        private String deletedKey;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();
            if ("putObject".equals(methodName)) {
                bucket = (String) args[0];
                key = (String) args[1];
                metadata = (ObjectMetadata) args[3];
                return null;
            }
            if ("doesObjectExist".equals(methodName)) {
                bucket = (String) args[0];
                key = (String) args[1];
                return exists;
            }
            if ("deleteObject".equals(methodName)) {
                deletedBucket = (String) args[0];
                deletedKey = (String) args[1];
                return null;
            }
            if ("toString".equals(methodName)) {
                return "S3Stub";
            }
            return defaultValue(method.getReturnType());
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            if (InputStream.class.isAssignableFrom(type)) {
                return new ByteArrayInputStream(new byte[0]);
            }
            return null;
        }
        if (Boolean.TYPE.equals(type)) {
            return false;
        }
        if (Integer.TYPE.equals(type)) {
            return 0;
        }
        if (Long.TYPE.equals(type)) {
            return 0L;
        }
        if (Float.TYPE.equals(type)) {
            return 0F;
        }
        if (Double.TYPE.equals(type)) {
            return 0D;
        }
        return null;
    }
}
