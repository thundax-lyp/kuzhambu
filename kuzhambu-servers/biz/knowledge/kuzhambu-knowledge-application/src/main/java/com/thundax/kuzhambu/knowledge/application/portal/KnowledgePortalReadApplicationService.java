package com.thundax.kuzhambu.knowledge.application.portal;

import com.thundax.kuzhambu.knowledge.application.lineage.query.LineageCanvasQuery;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult;

public interface KnowledgePortalReadApplicationService {

    KnowledgePortalHomeResult getHome();

    KnowledgePortalAtlasResult getAtlas(KnowledgePortalAtlasQuery query);

    LineageCanvasResult getLineage(LineageCanvasQuery query);

    KnowledgePortalQualityResult getQuality();
}
