package com.thundax.kuzhambu.knowledge.application.lineage.service;

import com.thundax.kuzhambu.knowledge.application.lineage.query.LineageCanvasQuery;
import com.thundax.kuzhambu.knowledge.application.lineage.result.LineageCanvasResult;

public interface KnowledgeLineageReadApplicationService {

    LineageCanvasResult getCanvas(LineageCanvasQuery query);

    LineageCanvasResult getLatestAppliedCanvas(LineageCanvasQuery query);
}
