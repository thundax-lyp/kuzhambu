package com.thundax.kuzhambu.knowledge.application.taxonomy.service;

import com.thundax.kuzhambu.knowledge.application.taxonomy.query.DiscoveryEntityHintQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.DiscoveryTagHintQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryEntityHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryTagHintResult;
import java.util.List;

public interface KnowledgeTaxonomyReadApplicationService {

    DiscoveryTagHintResult getTagHint(DiscoveryTagHintQuery query);

    List<DiscoveryEntityHintResult> listEntityHints(DiscoveryEntityHintQuery query);
}
