package com.thundax.kuzhambu.ai.facade.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiBatchJobPageFacadeResponse {

    private final int pageNo;
    private final int pageSize;
    private final long totalCount;
    private final int totalPage;
    private final List<AiBatchJobFacadeResponse> records;
}
