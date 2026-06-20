package com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsSharePortalListResponse implements Serializable {
    @JsonProperty("pageNo")
    private int pageNo;

    @JsonProperty("pageSize")
    private int pageSize;

    @JsonProperty("totalCount")
    private long totalCount;

    @JsonProperty("totalPage")
    private int totalPage;

    @JsonProperty("records")
    private List<ClassicsSharePortalListItemResponse> records;
}
