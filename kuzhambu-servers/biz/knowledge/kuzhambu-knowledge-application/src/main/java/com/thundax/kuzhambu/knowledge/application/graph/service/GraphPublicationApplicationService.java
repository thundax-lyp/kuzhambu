package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.knowledge.application.graph.command.GraphBatchPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphPublicationCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphWithdrawalCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphBatchPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphPublicationPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphWithdrawalPreviewQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchPublicationPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphBatchPublicationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWithdrawalPreviewResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;

public interface GraphPublicationApplicationService {

    GraphPublicationPreviewResult previewPublication(GraphPublicationPreviewQuery query);

    GraphBatchPublicationPreviewResult previewBatchPublication(GraphBatchPublicationPreviewQuery query);

    GraphPublicationResult publish(GraphPublicationCommand command);

    GraphBatchPublicationResult publishBatch(GraphBatchPublicationCommand command);

    GraphWithdrawalPreviewResult previewWithdrawal(GraphWithdrawalPreviewQuery query);

    GraphMaterial withdraw(GraphWithdrawalCommand command);
}
