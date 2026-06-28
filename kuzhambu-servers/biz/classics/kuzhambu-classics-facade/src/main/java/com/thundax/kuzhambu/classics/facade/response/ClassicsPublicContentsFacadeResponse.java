package com.thundax.kuzhambu.classics.facade.response;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsPublicContentsFacadeResponse {

    private final List<ClassicsPublicContentFacadeDto> contents;
}
