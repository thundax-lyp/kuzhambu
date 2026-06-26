package com.thundax.kuzhambu.operations.domain.report.client;

import com.thundax.kuzhambu.operations.domain.report.client.dto.OperationsWorkerRenderDtos;

public interface OperationsWorkerRenderClient {

    OperationsWorkerRenderDtos.WorkerRenderResponse renderOperationsReport(
            OperationsWorkerRenderDtos.WorkerRenderRequest request);
}
