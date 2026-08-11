package com.thundax.kuzhambu.knowledge.application.portal.service;

import com.thundax.kuzhambu.knowledge.application.lineage.query.LineageCanvasQuery;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult;
import com.thundax.kuzhambu.knowledge.application.portal.query.KnowledgePortalAtlasQuery;
import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalAtlasResult;
import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalHomeResult;
import com.thundax.kuzhambu.knowledge.application.portal.result.KnowledgePortalQualityResult;

public interface KnowledgePortalReadApplicationService {

    KnowledgePortalHomeResult getHome();

    KnowledgePortalAtlasResult getAtlas(KnowledgePortalAtlasQuery query);

    LineageCanvasResult getLineage(LineageCanvasQuery query);

    KnowledgePortalQualityResult getQuality();
}
