package com.thundax.kuzhambu.common.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class BizExceptionBoundaryAspect {

    public static final String TECHNICAL_FAILURE_CODE = "BIZ-00001";
    public static final String TECHNICAL_FAILURE_MESSAGE_KEY = "biz.exception.technical-failure";
    public static final String TECHNICAL_FAILURE_MESSAGE = "业务处理失败";

    @Around("(@within(com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary)"
            + " && !@annotation(com.thundax.kuzhambu.common.core.exception.BizExceptionBoundaryIgnore))"
            + " || @annotation(com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary)")
    public Object convertTechnicalException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (BizException exception) {
            throw exception;
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            log.warn("technical exception converted to BizException", throwable);
            throw new BizException(
                    TECHNICAL_FAILURE_CODE, TECHNICAL_FAILURE_MESSAGE_KEY, TECHNICAL_FAILURE_MESSAGE, throwable);
        }
    }
}
