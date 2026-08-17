package com.thundax.kuzhambu.knowledge.application.graph.command;

import java.util.List;

public record GraphBatchWithdrawalCommand(List<GraphWithdrawalCommand> materials, String idempotencyKey) {}
