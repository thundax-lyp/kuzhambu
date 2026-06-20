package com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsShareTargetRequest implements Serializable {
    @JsonProperty("contentType")
    @NotBlank(message = "contentType不能为空")
    private String contentType;

    @JsonProperty("contentId")
    @NotNull(message = "contentId不能为空")
    private Long contentId;
}
