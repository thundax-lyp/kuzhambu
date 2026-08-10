package com.thundax.kuzhambu.knowledge.interfaces.portal.home.assembler;

import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalHomeResult;
import com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.response.KnowledgePortalHomeResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class KnowledgePortalHomeInterfaceAssembler {

    private KnowledgePortalHomeInterfaceAssembler() {}

    @NonNull
    public static KnowledgePortalHomeResponse toResponse(@NonNull KnowledgePortalHomeResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return KnowledgePortalHomeResponse.builder()
                .heroTitle(result.getHeroTitle())
                .heroSubtitle(result.getHeroSubtitle())
                .searchPlaceholder(result.getSearchPlaceholder())
                .stats(toStatResponses(result.getStats()))
                .quickLinks(toQuickLinkResponses(result.getQuickLinks()))
                .recentUpdates(toRecentUpdateResponses(result.getRecentUpdates()))
                .featureCollections(toFeatureCollectionResponses(result.getFeatureCollections()))
                .build();
    }

    private static List<KnowledgePortalHomeResponse.PortalStatResponse> toStatResponses(
            List<KnowledgePortalHomeResult.PortalStatItem> stats) {
        if (stats == null || stats.isEmpty()) {
            return Collections.emptyList();
        }
        return stats.stream()
                .map(item -> KnowledgePortalHomeResponse.PortalStatResponse.builder()
                        .key(item.getKey())
                        .label(item.getLabel())
                        .value(item.getValue())
                        .deltaText(item.getDeltaText())
                        .trend(item.getTrend())
                        .icon(item.getIcon())
                        .build())
                .toList();
    }

    private static List<KnowledgePortalHomeResponse.PortalQuickLinkResponse> toQuickLinkResponses(
            List<KnowledgePortalHomeResult.PortalQuickLinkItem> quickLinks) {
        if (quickLinks == null || quickLinks.isEmpty()) {
            return Collections.emptyList();
        }
        return quickLinks.stream()
                .map(item -> KnowledgePortalHomeResponse.PortalQuickLinkResponse.builder()
                        .key(item.getKey())
                        .label(item.getLabel())
                        .description(item.getDescription())
                        .href(item.getHref())
                        .type(item.getType())
                        .build())
                .toList();
    }

    private static List<KnowledgePortalHomeResponse.PortalRecentUpdateResponse> toRecentUpdateResponses(
            List<KnowledgePortalHomeResult.PortalRecentUpdateItem> recentUpdates) {
        if (recentUpdates == null || recentUpdates.isEmpty()) {
            return Collections.emptyList();
        }
        return recentUpdates.stream()
                .map(item -> KnowledgePortalHomeResponse.PortalRecentUpdateResponse.builder()
                        .title(item.getTitle())
                        .subtitle(item.getSubtitle())
                        .summary(item.getSummary())
                        .updatedAt(item.getUpdatedAt())
                        .href(item.getHref())
                        .coverImageUrl(item.getCoverImageUrl())
                        .build())
                .toList();
    }

    private static List<KnowledgePortalHomeResponse.PortalFeatureCollectionResponse> toFeatureCollectionResponses(
            List<KnowledgePortalHomeResult.PortalFeatureCollectionItem> featureCollections) {
        if (featureCollections == null || featureCollections.isEmpty()) {
            return Collections.emptyList();
        }
        return featureCollections.stream()
                .map(item -> KnowledgePortalHomeResponse.PortalFeatureCollectionResponse.builder()
                        .key(item.getKey())
                        .label(item.getLabel())
                        .description(item.getDescription())
                        .href(item.getHref())
                        .badgeText(item.getBadgeText())
                        .build())
                .toList();
    }
}
