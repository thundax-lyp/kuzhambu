package com.thundax.kuzhambu.knowledge.interfaces.portal.home.assembler;

import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalHomeResult;
import com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.response.KnowledgePortalHomeResponse;
import java.util.Collections;
import java.util.List;

public final class KnowledgePortalHomeInterfaceAssembler {

    private KnowledgePortalHomeInterfaceAssembler() {}

    public static KnowledgePortalHomeResponse toResponse(KnowledgePortalHomeResult result) {
        if (result == null) {
            return null;
        }
        KnowledgePortalHomeResponse response = new KnowledgePortalHomeResponse();
        response.setHeroTitle(result.getHeroTitle());
        response.setHeroSubtitle(result.getHeroSubtitle());
        response.setSearchPlaceholder(result.getSearchPlaceholder());
        response.setStats(toStatResponses(result.getStats()));
        response.setQuickLinks(toQuickLinkResponses(result.getQuickLinks()));
        response.setRecentUpdates(toRecentUpdateResponses(result.getRecentUpdates()));
        response.setFeatureCollections(toFeatureCollectionResponses(result.getFeatureCollections()));
        return response;
    }

    private static List<KnowledgePortalHomeResponse.PortalStatResponse> toStatResponses(
            List<KnowledgePortalHomeResult.PortalStatItem> stats) {
        if (stats == null || stats.isEmpty()) {
            return Collections.emptyList();
        }
        return stats.stream()
                .map(item -> new KnowledgePortalHomeResponse.PortalStatResponse(
                        item.getKey(),
                        item.getLabel(),
                        item.getValue(),
                        item.getDeltaText(),
                        item.getTrend(),
                        item.getIcon()))
                .toList();
    }

    private static List<KnowledgePortalHomeResponse.PortalQuickLinkResponse> toQuickLinkResponses(
            List<KnowledgePortalHomeResult.PortalQuickLinkItem> quickLinks) {
        if (quickLinks == null || quickLinks.isEmpty()) {
            return Collections.emptyList();
        }
        return quickLinks.stream()
                .map(item -> new KnowledgePortalHomeResponse.PortalQuickLinkResponse(
                        item.getKey(), item.getLabel(), item.getDescription(), item.getHref(), item.getType()))
                .toList();
    }

    private static List<KnowledgePortalHomeResponse.PortalRecentUpdateResponse> toRecentUpdateResponses(
            List<KnowledgePortalHomeResult.PortalRecentUpdateItem> recentUpdates) {
        if (recentUpdates == null || recentUpdates.isEmpty()) {
            return Collections.emptyList();
        }
        return recentUpdates.stream()
                .map(item -> new KnowledgePortalHomeResponse.PortalRecentUpdateResponse(
                        item.getTitle(),
                        item.getSubtitle(),
                        item.getSummary(),
                        item.getUpdatedAt(),
                        item.getHref(),
                        item.getCoverImageUrl()))
                .toList();
    }

    private static List<KnowledgePortalHomeResponse.PortalFeatureCollectionResponse> toFeatureCollectionResponses(
            List<KnowledgePortalHomeResult.PortalFeatureCollectionItem> featureCollections) {
        if (featureCollections == null || featureCollections.isEmpty()) {
            return Collections.emptyList();
        }
        return featureCollections.stream()
                .map(item -> new KnowledgePortalHomeResponse.PortalFeatureCollectionResponse(
                        item.getKey(), item.getLabel(), item.getDescription(), item.getHref(), item.getBadgeText()))
                .toList();
    }
}
