package com.thundax.kuzhambu.discovery.application.facade.assembler;

import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationPrepareCommand;
import com.thundax.kuzhambu.discovery.application.search.command.SearchPublicationReferenceCommand;
import com.thundax.kuzhambu.discovery.application.search.result.SearchPublicationProbeResult;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationPrepareFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationReferenceFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationProbeFacadeResponse;
import org.springframework.stereotype.Component;

@Component
public class DiscoverySearchPublicationFacadeAssembler {

    public SearchPublicationPrepareCommand toPrepareCommand(DiscoverySearchPublicationPrepareFacadeRequest request) {
        return new SearchPublicationPrepareCommand(
                request.getSourceId(),
                request.getContentType(),
                request.getContentId(),
                request.getContentVersionId(),
                request.getContentVersionNo(),
                request.getTitle(),
                request.getSummary(),
                request.getCategoryId(),
                request.getCategoryName(),
                request.getVolumeId(),
                request.getVolumeTitle(),
                request.getTextSegments(),
                request.getTagNames(),
                request.getContentUpdatedAt());
    }

    public SearchPublicationReferenceCommand toReferenceCommand(
            DiscoverySearchPublicationReferenceFacadeRequest request) {
        return new SearchPublicationReferenceCommand(
                request.getDocumentId(),
                request.getContentVersionId(),
                request.getContentVersionNo(),
                request.getOccurredAt());
    }

    public DiscoverySearchPublicationProbeFacadeResponse toProbeResponse(SearchPublicationProbeResult result) {
        return DiscoverySearchPublicationProbeFacadeResponse.builder()
                .present(result.isPresent())
                .publicationStatus(result.getPublicationStatus())
                .deleted(result.getDeleted())
                .contentVersionId(result.getContentVersionId())
                .contentVersionNo(result.getContentVersionNo())
                .build();
    }
}
