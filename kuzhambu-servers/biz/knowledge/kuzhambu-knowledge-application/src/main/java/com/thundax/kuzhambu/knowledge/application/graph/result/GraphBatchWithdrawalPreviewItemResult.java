package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;

public record GraphBatchWithdrawalPreviewItemResult(
        ContentRef contentRef, GraphWithdrawalPreviewResult preview, String failureCode, String failureMessage) {}
