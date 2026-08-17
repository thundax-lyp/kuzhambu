package com.thundax.kuzhambu.knowledge.application.graph.result;

import java.util.List;

public record GraphBatchWithdrawalResult(String batchId, List<GraphWithdrawalResult> materials) {}
