package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

public record ClassicsPublicationBatchItemResponse(Long contentId, boolean accepted, Long jobId, String reason) {}
