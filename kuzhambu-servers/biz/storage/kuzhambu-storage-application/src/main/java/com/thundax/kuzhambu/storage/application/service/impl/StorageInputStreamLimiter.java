package com.thundax.kuzhambu.storage.application.service.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

final class StorageInputStreamLimiter {

    private StorageInputStreamLimiter() {}

    static InputStream limit(InputStream inputStream, long maxBytes) {
        return new LimitedInputStream(inputStream, maxBytes);
    }

    private static final class LimitedInputStream extends FilterInputStream {

        private final long maxBytes;
        private long readBytes;

        private LimitedInputStream(InputStream inputStream, long maxBytes) {
            super(inputStream);
            this.maxBytes = maxBytes;
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value != -1) {
                count(1);
            }
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int count = super.read(buffer, offset, length);
            if (count > 0) {
                count(count);
            }
            return count;
        }

        private void count(long count) throws IOException {
            readBytes += count;
            if (readBytes > maxBytes) {
                throw new IOException("文件大小超过声明大小");
            }
        }
    }
}
