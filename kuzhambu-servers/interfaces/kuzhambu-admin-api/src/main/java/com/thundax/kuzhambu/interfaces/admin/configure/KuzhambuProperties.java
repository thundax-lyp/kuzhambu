package com.thundax.kuzhambu.interfaces.admin.configure;

import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

@ConfigurationProperties(prefix = "kuzhambu")
public class KuzhambuProperties {

    private static final int DEFAULT_LOG_ALIVE_DAYS = 90;
    private static final String DEFAULT_LOG_STORAGE_PATH =
            System.getProperty("java.io.tmpdir") + File.separator + "kuzhambu" + File.separator + "sys-log";

    @Setter
    private LogProperties log;

    @Setter
    private UploadProperties upload;

    @Setter
    private AccessTokenFilterProperties accessTokenFilter;

    @NonNull
    public LogProperties getLog() {
        return log != null ? log : new LogProperties();
    }

    @NonNull
    public UploadProperties getUpload() {
        return upload != null ? upload : new UploadProperties();
    }

    @NotNull
    public AccessTokenFilterProperties getAccessTokenFilter() {
        return accessTokenFilter != null ? accessTokenFilter : new AccessTokenFilterProperties();
    }

    public static class UploadProperties {

        @Setter
        private String contentPath;

        @Setter
        private String storagePath;

        @Setter
        private List<String> allowImageSuffix;

        @Setter
        private List<String> allowSuffix;

        @Setter
        private Integer maxFileCount;

        @Setter
        private Long maxFileSize;

        @Setter
        private Float imageQuality;

        public String getContentPath() {
            if (StringUtils.isEmpty(contentPath)) {
                return "/api/storage/object/";
            }
            return contentPath.endsWith("/") ? contentPath : contentPath + "/";
        }

        @NonNull
        public String getStoragePath() {
            if (StringUtils.isBlank(storagePath)) {
                return File.separator;
            }
            if (!StringUtils.endsWith(storagePath, File.separator)) {
                return storagePath + File.separator;
            }
            return storagePath;
        }

        @NonNull
        public List<String> getAllowImageSuffix() {
            return allowImageSuffix == null ? new ArrayList<>() : allowImageSuffix;
        }

        @NonNull
        public String getAllowImageSuffixString() {
            return StringUtils.join(getAllowImageSuffix(), ",");
        }

        @NonNull
        public List<String> getAllowSuffix() {
            return allowSuffix == null ? new ArrayList<>() : allowSuffix;
        }

        @NonNull
        public String getAllowSuffixString() {
            return StringUtils.join(getAllowSuffix(), ",");
        }

        @NonNull
        public Integer getMaxFileCount() {
            return maxFileCount == null || maxFileCount < 0 ? 10 : maxFileCount;
        }

        @NonNull
        public Long getMaxFileSize() {
            return maxFileSize == null || maxFileSize < 0L ? 20971520L : maxFileSize;
        }

        @NonNull
        public Float getImageQuality() {
            return imageQuality == null || imageQuality < 0.5 ? 0.8f : imageQuality;
        }
    }

    public static class LogProperties {

        @Setter
        private String storagePath;

        @Setter
        private Integer aliveDays;

        @Setter
        private SysLogProperties sys;

        @NonNull
        public String getStoragePath() {
            if (StringUtils.isBlank(storagePath)) {
                return DEFAULT_LOG_STORAGE_PATH + File.separator;
            }
            if (!StringUtils.endsWith(storagePath, File.separator)) {
                return storagePath + File.separator;
            }
            return storagePath;
        }

        @NonNull
        public Integer getAliveDays() {
            return aliveDays == null ? DEFAULT_LOG_ALIVE_DAYS : aliveDays;
        }

        @NonNull
        public SysLogProperties getSys() {
            return sys != null ? sys : new SysLogProperties();
        }
    }

    public static class SysLogProperties {

        private static final String DEFAULT_QUEUE = "kuzhambu.save-log";
        private static final String DEFAULT_TOPIC = "kuzhambu_save_log";

        @Setter
        private String queue;

        @Setter
        private String topic;

        @Setter
        private String tag;

        @NonNull
        public String getQueue() {
            return StringUtils.defaultIfBlank(queue, DEFAULT_QUEUE);
        }

        @NonNull
        public String getTopic() {
            return StringUtils.defaultIfBlank(topic, DEFAULT_TOPIC);
        }

        public String getTag() {
            if (StringUtils.isBlank(tag) || "*".equals(tag)) {
                return null;
            }
            return tag;
        }
    }

    public static class AccessTokenFilterProperties {

        @Setter
        private List<String> urlPatterns;

        @Setter
        private List<String> excludePath;

        public List<String> getUrlPatterns() {
            return urlPatterns;
        }

        public List<String> getExcludePath() {
            return excludePath == null ? new ArrayList<>() : excludePath;
        }
    }
}
