package com.thundax.kuzhambu.common.web.context;

import jakarta.servlet.http.HttpServletRequest;

public interface KuzhambuContextResolver {

    String resolveRequestId(HttpServletRequest request);
}
