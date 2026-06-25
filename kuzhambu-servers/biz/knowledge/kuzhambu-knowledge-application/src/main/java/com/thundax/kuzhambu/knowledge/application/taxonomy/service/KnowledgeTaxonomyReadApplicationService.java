package com.thundax.kuzhambu.knowledge.application.taxonomy.service;

import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryEntityHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoverySynonymExpandResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryTagHintResult;
import java.util.List;

public interface KnowledgeTaxonomyReadApplicationService {

    DiscoverySynonymExpandResult expandSynonyms(String term);

    DiscoveryTagHintResult getTagHint(String term);

    List<DiscoveryEntityHintResult> listEntityHints(String term);
}
