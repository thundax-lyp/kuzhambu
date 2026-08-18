package com.thundax.kuzhambu.knowledge.domain.graph.model;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;

public record GraphExtractionTaskWithMaterial(GraphExtractionTask task, String materialTitle) {}
