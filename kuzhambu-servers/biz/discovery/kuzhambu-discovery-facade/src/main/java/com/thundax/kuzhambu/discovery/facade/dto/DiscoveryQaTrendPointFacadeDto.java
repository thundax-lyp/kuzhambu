package com.thundax.kuzhambu.discovery.facade.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscoveryQaTrendPointFacadeDto {

    private final String bucket;
    private final Long qaCount;
}
