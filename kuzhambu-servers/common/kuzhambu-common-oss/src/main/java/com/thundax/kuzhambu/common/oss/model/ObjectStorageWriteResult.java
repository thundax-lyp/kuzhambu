package com.thundax.kuzhambu.common.oss.model;

public class ObjectStorageWriteResult {

    private String key;
    private String location;
    private long size;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
