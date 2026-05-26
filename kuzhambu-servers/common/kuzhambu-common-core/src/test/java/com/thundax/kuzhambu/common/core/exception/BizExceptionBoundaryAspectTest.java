package com.thundax.kuzhambu.common.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

public class BizExceptionBoundaryAspectTest {

    private final BizExceptionBoundaryAspect aspect = new BizExceptionBoundaryAspect();

    @Test
    public void shouldReturnProceedResult() throws Throwable {
        Object result = aspect.convertTechnicalException(new FixedProceedingJoinPoint("ok"));

        assertEquals("ok", result);
    }

    @Test
    public void shouldPassThroughBizException() throws Throwable {
        BizException expected = new BizException("AUTH-00001", "auth.exception.invalid-captcha", "验证码错误");

        try {
            aspect.convertTechnicalException(new ThrowingProceedingJoinPoint(expected));
            fail("BizException should be thrown");
        } catch (BizException actual) {
            assertSame(expected, actual);
        }
    }

    @Test
    public void shouldPassThroughDomainException() throws Throwable {
        DomainException expected = new DomainException("MEMBER-00002", "member.exception.disabled", "会员状态不可用");

        try {
            aspect.convertTechnicalException(new ThrowingProceedingJoinPoint(expected));
            fail("DomainException should be thrown");
        } catch (DomainException actual) {
            assertSame(expected, actual);
        }
    }

    @Test
    public void shouldConvertTechnicalExceptionToBizException() throws Throwable {
        IllegalStateException expectedCause = new IllegalStateException("database timeout");

        try {
            aspect.convertTechnicalException(new ThrowingProceedingJoinPoint(expectedCause));
            fail("BizException should be thrown");
        } catch (BizException actual) {
            assertEquals(BizExceptionBoundaryAspect.TECHNICAL_FAILURE_CODE, actual.getCode());
            assertEquals(BizExceptionBoundaryAspect.TECHNICAL_FAILURE_MESSAGE_KEY, actual.getMessageKey());
            assertEquals(BizExceptionBoundaryAspect.TECHNICAL_FAILURE_MESSAGE, actual.getDefaultMessage());
            assertSame(expectedCause, actual.getCause());
        }
    }

    @Test
    public void shouldPassThroughError() throws Throwable {
        AssertionError expected = new AssertionError("fatal");

        try {
            aspect.convertTechnicalException(new ThrowingProceedingJoinPoint(expected));
            fail("Error should be thrown");
        } catch (AssertionError actual) {
            assertSame(expected, actual);
        }
    }

    @Test
    public void shouldDeclareIgnoreAnnotationOnGetterMethods() throws Exception {
        Method method = IgnoredGetterService.class.getMethod("getStatus");

        assertTrue(method.isAnnotationPresent(BizExceptionBoundaryIgnore.class));
    }

    private static class IgnoredGetterService {

        @BizExceptionBoundaryIgnore
        public String getStatus() {
            return "ok";
        }
    }

    private static class FixedProceedingJoinPoint extends UnsupportedProceedingJoinPoint {

        private final Object result;

        FixedProceedingJoinPoint(Object result) {
            this.result = result;
        }

        @Override
        public Object proceed() {
            return result;
        }
    }

    private static class ThrowingProceedingJoinPoint extends UnsupportedProceedingJoinPoint {

        private final Throwable throwable;

        ThrowingProceedingJoinPoint(Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public Object proceed() throws Throwable {
            throw throwable;
        }
    }

    private abstract static class UnsupportedProceedingJoinPoint implements ProceedingJoinPoint {

        @Override
        public Object proceed(Object[] args) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toShortString() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toLongString() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getThis() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getTarget() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object[] getArgs() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.aspectj.lang.Signature getSignature() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.aspectj.lang.reflect.SourceLocation getSourceLocation() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getKind() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.aspectj.lang.JoinPoint.StaticPart getStaticPart() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set$AroundClosure(org.aspectj.runtime.internal.AroundClosure aroundClosure) {
            throw new UnsupportedOperationException();
        }
    }
}
