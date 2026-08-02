package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

import java.util.List;

public record ClassicsPublicationBatchResponse(
        long acceptedCount, long rejectedCount, List<ClassicsPublicationBatchItemResponse> items) {}
