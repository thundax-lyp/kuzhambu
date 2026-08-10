package com.thundax.kuzhambu.knowledge.application.taxonomy.service.impl;

import com.thundax.kuzhambu.knowledge.application.taxonomy.query.DiscoveryEntityHintQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.DiscoveryTagHintQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryEntityHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryTagHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.KnowledgeTaxonomyReadApplicationService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class KnowledgeTaxonomyReadApplicationServiceImpl implements KnowledgeTaxonomyReadApplicationService {

    private final TagRepository tagRepository;
    private final TagAliasRepository tagAliasRepository;
    private final TagContentRefRepository tagContentRefRepository;

    public KnowledgeTaxonomyReadApplicationServiceImpl(
            TagRepository tagRepository,
            TagAliasRepository tagAliasRepository,
            TagContentRefRepository tagContentRefRepository) {
        this.tagRepository = tagRepository;
        this.tagAliasRepository = tagAliasRepository;
        this.tagContentRefRepository = tagContentRefRepository;
    }

    @Override
    public DiscoveryTagHintResult getTagHint(DiscoveryTagHintQuery query) {
        DiscoveryTagHintQuery effectiveQuery = query == null ? new DiscoveryTagHintQuery(null) : query;
        String normalizedTerm = normalizeTerm(effectiveQuery.term());
        if (normalizedTerm == null) {
            return new DiscoveryTagHintResult(effectiveQuery.term(), null, null, null, 0L);
        }

        Tag directTag = tagRepository.getByName(normalizedTerm);
        if (directTag != null && directTag.getId() != null) {
            return new DiscoveryTagHintResult(effectiveQuery.term(), normalizedTerm, directTag.getName(), null, (long)
                    tagContentRefRepository.countByTagId(directTag.getId()));
        }

        TagAlias alias = tagAliasRepository.getByName(normalizedTerm);
        if (alias == null || alias.getTagId() == null) {
            return new DiscoveryTagHintResult(effectiveQuery.term(), normalizedTerm, null, null, 0L);
        }
        Tag targetTag = tagRepository.getByTagId(alias.getTagId());
        Long contentRefCount = targetTag == null || targetTag.getId() == null
                ? 0L
                : (long) tagContentRefRepository.countByTagId(targetTag.getId());
        return new DiscoveryTagHintResult(
                effectiveQuery.term(),
                normalizedTerm,
                targetTag == null ? null : targetTag.getName(),
                alias.getName(),
                contentRefCount);
    }

    @Override
    public List<DiscoveryEntityHintResult> listEntityHints(DiscoveryEntityHintQuery query) {
        return List.of();
    }

    private String normalizeTerm(String term) {
        return StringUtils.trimToNull(term);
    }
}
