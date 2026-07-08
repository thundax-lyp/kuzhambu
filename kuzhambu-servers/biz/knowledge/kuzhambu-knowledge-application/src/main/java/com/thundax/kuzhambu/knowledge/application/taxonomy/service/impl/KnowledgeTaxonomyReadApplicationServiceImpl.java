package com.thundax.kuzhambu.knowledge.application.taxonomy.service.impl;

import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryEntityHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoverySynonymExpandResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoverySynonymMatchResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoverySynonymQueryResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryTagHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.KnowledgeTaxonomyReadApplicationService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Synonym;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.SynonymStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.SynonymRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class KnowledgeTaxonomyReadApplicationServiceImpl implements KnowledgeTaxonomyReadApplicationService {

    private static final int DEFAULT_EXPAND_LIMIT = 50;
    private static final String DIRECTION_FORWARD = "FORWARD";
    private static final String DIRECTION_REVERSE = "REVERSE";
    private static final String DIRECTION_BIDIRECTIONAL = "BIDIRECTIONAL";

    private final SynonymRepository synonymRepository;
    private final TagRepository tagRepository;
    private final TagAliasRepository tagAliasRepository;
    private final TagContentRefRepository tagContentRefRepository;

    public KnowledgeTaxonomyReadApplicationServiceImpl(
            SynonymRepository synonymRepository,
            TagRepository tagRepository,
            TagAliasRepository tagAliasRepository,
            TagContentRefRepository tagContentRefRepository) {
        this.synonymRepository = synonymRepository;
        this.tagRepository = tagRepository;
        this.tagAliasRepository = tagAliasRepository;
        this.tagContentRefRepository = tagContentRefRepository;
    }

    @Override
    public DiscoverySynonymQueryResult querySynonyms(String term, String direction, Integer limit) {
        String normalizedTerm = normalizeTerm(term);
        String normalizedDirection = normalizeDirection(direction);
        int adjustedLimit = clampLimit(limit);

        if (normalizedTerm == null) {
            return new DiscoverySynonymQueryResult(term, null, normalizedDirection, adjustedLimit, List.of());
        }

        Set<String> expandedTerms = new LinkedHashSet<>();
        List<DiscoverySynonymMatchResult> matches = new ArrayList<>();
        if (DIRECTION_FORWARD.equals(normalizedDirection) || DIRECTION_BIDIRECTIONAL.equals(normalizedDirection)) {
            collectSynonyms(
                    synonymRepository
                            .page(normalizedTerm, null, SynonymStatus.ENABLED, 1, adjustedLimit)
                            .getRecords(),
                    normalizedTerm,
                    DIRECTION_FORWARD,
                    expandedTerms,
                    matches);
        }
        if (DIRECTION_REVERSE.equals(normalizedDirection) || DIRECTION_BIDIRECTIONAL.equals(normalizedDirection)) {
            collectSynonyms(
                    synonymRepository
                            .page(null, normalizedTerm, SynonymStatus.ENABLED, 1, adjustedLimit)
                            .getRecords(),
                    normalizedTerm,
                    DIRECTION_REVERSE,
                    expandedTerms,
                    matches);
        }

        return new DiscoverySynonymQueryResult(
                term, normalizedTerm, normalizedDirection, adjustedLimit, List.copyOf(matches));
    }

    @Override
    public DiscoverySynonymExpandResult expandSynonyms(String term) {
        DiscoverySynonymQueryResult queryResult = querySynonyms(term, DIRECTION_BIDIRECTIONAL, null);
        List<String> expandedTerms = queryResult.getMatches() == null
                ? List.of()
                : queryResult.getMatches().stream()
                        .map(DiscoverySynonymMatchResult::getExpandedTerm)
                        .toList();
        return new DiscoverySynonymExpandResult(queryResult.getTerm(), queryResult.getNormalizedTerm(), expandedTerms);
    }

    @Override
    public DiscoveryTagHintResult getTagHint(String term) {
        String normalizedTerm = normalizeTerm(term);
        if (normalizedTerm == null) {
            return new DiscoveryTagHintResult(term, null, null, null, 0L);
        }

        Tag directTag = tagRepository.getByName(normalizedTerm);
        if (directTag != null && directTag.getTagId() != null) {
            return new DiscoveryTagHintResult(term, normalizedTerm, directTag.getName(), null, (long)
                    tagContentRefRepository.countByTagId(directTag.getTagId()));
        }

        TagAlias alias = tagAliasRepository.getByName(normalizedTerm);
        if (alias == null || alias.getTagId() == null) {
            return new DiscoveryTagHintResult(term, normalizedTerm, null, null, 0L);
        }
        Tag targetTag = tagRepository.getByTagId(alias.getTagId());
        Long contentRefCount = targetTag == null || targetTag.getTagId() == null
                ? 0L
                : (long) tagContentRefRepository.countByTagId(targetTag.getTagId());
        return new DiscoveryTagHintResult(
                term, normalizedTerm, targetTag == null ? null : targetTag.getName(), alias.getName(), contentRefCount);
    }

    @Override
    public List<DiscoveryEntityHintResult> listEntityHints(String term) {
        return List.of();
    }

    private String normalizeTerm(String term) {
        return StringUtils.trimToNull(term);
    }

    private int clampLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_EXPAND_LIMIT;
        }
        return Math.min(limit, DEFAULT_EXPAND_LIMIT);
    }

    private String normalizeDirection(String direction) {
        return switch (StringUtils.trimToEmpty(direction).toUpperCase()) {
            case DIRECTION_FORWARD -> DIRECTION_FORWARD;
            case DIRECTION_REVERSE -> DIRECTION_REVERSE;
            default -> DIRECTION_BIDIRECTIONAL;
        };
    }

    private void collectSynonyms(
            List<Synonym> synonyms,
            String normalizedTerm,
            String direction,
            Set<String> expandedTerms,
            List<DiscoverySynonymMatchResult> matches) {
        if (synonyms == null) {
            return;
        }

        for (Synonym synonym : synonyms) {
            String sourceTerm = StringUtils.trimToNull(synonym.getTerm());
            String targetTerm = StringUtils.trimToNull(synonym.getSynonym());
            if (sourceTerm == null || targetTerm == null) {
                continue;
            }

            String matchedTerm = DIRECTION_REVERSE.equals(direction) ? targetTerm : sourceTerm;
            String expandedTerm = DIRECTION_REVERSE.equals(direction) ? sourceTerm : targetTerm;
            String normalizedExpandedTerm = normalizeTerm(expandedTerm);
            if (normalizedExpandedTerm == null) {
                continue;
            }
            if (StringUtils.equals(normalizedExpandedTerm, normalizedTerm)) {
                continue;
            }

            if (expandedTerms.add(normalizedExpandedTerm)) {
                matches.add(new DiscoverySynonymMatchResult(
                        sourceTerm, targetTerm, matchedTerm, normalizedExpandedTerm, direction));
            }
        }
    }
}
