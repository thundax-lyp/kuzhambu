package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublicationPreviewToken;
import java.time.Instant;

public interface GraphPublicationPreviewTokenRepository {

    GraphPublicationPreviewToken getByToken(String token);

    String insert(GraphPublicationPreviewToken token);

    GraphPublicationPreviewToken updateConsumedAtIfAvailable(String token, Instant consumedAt);
}
