package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

public record ClassicsPublicationCreateResponse(
        Long jobId, String contentType, Long contentId, String lifecycleStatus, String transitionStatus) {}
