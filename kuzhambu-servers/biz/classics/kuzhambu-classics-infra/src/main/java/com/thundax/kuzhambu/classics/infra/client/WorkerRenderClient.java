package com.thundax.kuzhambu.classics.infra.client;

import com.thundax.kuzhambu.classics.infra.client.dto.WorkerRenderDtos;

public interface WorkerRenderClient {

    WorkerRenderDtos.WorkerRenderResponse renderClassicsExport(WorkerRenderDtos.WorkerRenderRequest request);

    WorkerRenderDtos.WorkerRenderResponse renderSancaiShowcase(WorkerRenderDtos.WorkerRenderRequest request);
}
