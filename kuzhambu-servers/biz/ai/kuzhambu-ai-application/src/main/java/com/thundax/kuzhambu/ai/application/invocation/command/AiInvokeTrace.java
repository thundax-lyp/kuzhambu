package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;

public record AiInvokeTrace(RequestId requestId, TraceId traceId) {}
