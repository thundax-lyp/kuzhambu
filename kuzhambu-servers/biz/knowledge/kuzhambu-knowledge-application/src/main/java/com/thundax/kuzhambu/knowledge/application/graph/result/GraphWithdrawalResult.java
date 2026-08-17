package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;

public record GraphWithdrawalResult(
        ContentRef contentRef, boolean success, GraphMaterial result, String failureCode, String failureMessage) {}
