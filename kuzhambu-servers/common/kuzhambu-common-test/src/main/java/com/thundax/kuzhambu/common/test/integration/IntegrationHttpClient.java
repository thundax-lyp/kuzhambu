package com.thundax.kuzhambu.common.test.integration;

import java.util.Collections;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

public class IntegrationHttpClient {

    private final String baseUrl;
    private final RestOperations restOperations;

    public IntegrationHttpClient(String baseUrl) {
        this(baseUrl, new RestTemplate());
    }

    public IntegrationHttpClient(String baseUrl, RestOperations restOperations) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.restOperations = restOperations;
    }

    public <T> T get(String path, Class<T> responseType) {
        ResponseEntity<T> response = restOperations.getForEntity(url(path), responseType);
        return response.getBody();
    }

    public <T> T get(String path, Map<String, String> headerValues, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        addHeaders(headers, headerValues);
        ResponseEntity<T> response =
                restOperations.exchange(url(path), HttpMethod.GET, new HttpEntity<>(headers), responseType);
        return response.getBody();
    }

    public <T> T postJson(String path, Object body, Class<T> responseType) {
        return postJson(path, body, null, responseType);
    }

    public <T> T postJson(String path, Object body, Map<String, String> headerValues, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        addHeaders(headers, headerValues);
        return restOperations.postForObject(url(path), new HttpEntity<Object>(body, headers), responseType);
    }

    public <T> T postMultipart(String path, MultiValueMap<String, Object> parts, Class<T> responseType) {
        return postMultipart(path, parts, null, responseType);
    }

    public <T> T postMultipart(
            String path, MultiValueMap<String, Object> parts, Map<String, String> headerValues, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        addHeaders(headers, headerValues);
        return restOperations.postForObject(
                url(path), new HttpEntity<MultiValueMap<String, Object>>(parts, headers), responseType);
    }

    public MultiValueMap<String, Object> multipartBody(
            Map<String, ?> fields, String fileName, byte[] content, MediaType contentType) {
        return multipartBody(fields, "file", fileName, content, contentType);
    }

    public MultiValueMap<String, Object> multipartBody(
            Map<String, ?> fields, String fileField, String fileName, byte[] content, MediaType contentType) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        if (fields != null) {
            for (Map.Entry<String, ?> entry : fields.entrySet()) {
                body.add(entry.getKey(), entry.getValue());
            }
        }
        if (fileName != null && content != null) {
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : contentType);
            body.add(
                    fileField == null ? "file" : fileField,
                    new HttpEntity<Resource>(namedResource(fileName, content), fileHeaders));
        }
        return body;
    }

    private String url(String path) {
        if (path == null || path.length() == 0) {
            return baseUrl;
        }
        return path.charAt(0) == '/' ? baseUrl + path : baseUrl + "/" + path;
    }

    private static Resource namedResource(final String fileName, byte[] content) {
        return new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.length() == 0 || !value.endsWith("/")) {
            return value;
        }
        return value.substring(0, value.length() - 1);
    }

    private static void addHeaders(HttpHeaders headers, Map<String, String> headerValues) {
        if (headerValues == null || headerValues.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : headerValues.entrySet()) {
            headers.set(entry.getKey(), entry.getValue());
        }
    }
}
