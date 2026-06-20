package com.thundax.kuzhambu.classics.application.sharing.configure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.classics.share")
public class ClassicsShareProperties {
    private String portalWebBaseUrl = "http://localhost:5174";

    public String buildShareUrl(String shareToken) {
        String baseUrl =
                portalWebBaseUrl == null || portalWebBaseUrl.isBlank() ? "http://localhost:5174" : portalWebBaseUrl;
        return stripTrailingSlash(baseUrl) + "/share/" + shareToken;
    }

    private static String stripTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
