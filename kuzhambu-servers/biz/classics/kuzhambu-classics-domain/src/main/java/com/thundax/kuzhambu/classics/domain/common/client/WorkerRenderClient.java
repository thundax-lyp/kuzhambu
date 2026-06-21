package com.thundax.kuzhambu.classics.domain.common.client;

import com.thundax.kuzhambu.classics.domain.common.client.dto.WorkerRenderDtos;

public interface WorkerRenderClient {

    WorkerRenderDtos.WorkerRenderResponse renderClassicsExport(WorkerRenderDtos.WorkerRenderRequest request);

    WorkerRenderDtos.WorkerRenderResponse renderSancaiShowcase(WorkerRenderDtos.WorkerRenderRequest request);
}
