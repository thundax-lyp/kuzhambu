package com.thundax.kuzhambu.system.interfaces.admin.auth.security.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseBodyWrapper {

    private String code;
    private String message;
    private Object data;

    public ResponseBodyWrapper(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
