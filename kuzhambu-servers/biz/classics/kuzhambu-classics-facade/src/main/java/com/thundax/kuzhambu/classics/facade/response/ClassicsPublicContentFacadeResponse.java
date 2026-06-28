package com.thundax.kuzhambu.classics.facade.response;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsPublicContentFacadeResponse {

    private final ClassicsPublicContentFacadeDto content;
}
