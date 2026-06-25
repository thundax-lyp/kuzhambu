package com.thundax.kuzhambu.knowledge.application.taxonomy.service.impl;

import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryEntityHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoverySynonymExpandResult;
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
    public DiscoverySynonymExpandResult expandSynonyms(String term) {
        String normalizedTerm = normalizeTerm(term);
        if (normalizedTerm == null) {
            return new DiscoverySynonymExpandResult(term, null, List.of());
        }

        Set<String> expandedTerms = new LinkedHashSet<>();
        collectSynonyms(
                synonymRepository
                        .page(normalizedTerm, null, SynonymStatus.ENABLED, 1, DEFAULT_EXPAND_LIMIT)
                        .getRecords(),
                Synonym::getSynonym,
                expandedTerms);
        collectSynonyms(
                synonymRepository
                        .page(null, normalizedTerm, SynonymStatus.ENABLED, 1, DEFAULT_EXPAND_LIMIT)
                        .getRecords(),
                Synonym::getTerm,
                expandedTerms);
        expandedTerms.remove(normalizedTerm);
        return new DiscoverySynonymExpandResult(term, normalizedTerm, new ArrayList<>(expandedTerms));
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

    private void collectSynonyms(
            List<Synonym> synonyms,
            java.util.function.Function<Synonym, String> valueExtractor,
            Set<String> expandedTerms) {
        if (synonyms == null) {
            return;
        }
        synonyms.stream()
                .map(valueExtractor)
                .map(StringUtils::trimToNull)
                .filter(value -> value != null)
                .forEach(expandedTerms::add);
    }
}
