package com.thundax.kuzhambu.common.web;

import com.thundax.kuzhambu.common.web.util.RequestIpUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * 执行时间过滤器
 */
@Slf4j
public class ProcessTimeFilter implements Filter {

    public static final String START_TIME = "_start_time";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter.default 在tomcat中并没有起作用，大概是和版本有关
    }

    @Override
    public void destroy() {
        // Filter.default 在tomcat中并没有起作用，大概是和版本有关
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        String remoteAddr = RequestIpUtils.getIpAddr(request);

        long time = System.currentTimeMillis();
        request.setAttribute(START_TIME, time);

        chain.doFilter(request, response);

        time = System.currentTimeMillis() - time;
        log.debug("process {} ms[{}] from[{}]", request.getRequestURI(), time, remoteAddr);
    }
}
