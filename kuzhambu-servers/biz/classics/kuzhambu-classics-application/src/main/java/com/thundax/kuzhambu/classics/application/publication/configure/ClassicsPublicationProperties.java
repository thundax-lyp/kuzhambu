package com.thundax.kuzhambu.classics.application.publication.configure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kuzhambu.classics.publication")
public class ClassicsPublicationProperties {
    private boolean enabled;
    private Duration dispatchFixedDelay = Duration.ofSeconds(5);
    private Duration successReconcileFixedDelay = Duration.ofSeconds(30);
    private Duration failureReconcileFixedDelay = Duration.ofSeconds(30);
    private Duration esCleanupFixedDelay = Duration.ofSeconds(60);
    private Duration fastgptCleanupFixedDelay = Duration.ofSeconds(60);
    private Duration dispatchLease = Duration.ofSeconds(30);
    private Duration sliceLease = Duration.ofMinutes(10);
    private Duration cleanupLease = Duration.ofMinutes(5);
    private Duration retryDelay = Duration.ofSeconds(30);
    private int claimLimit = 20;
    private int executorCoreSize = 2;
    private int executorMaxSize = 4;
    private int executorQueueCapacity = 100;
    private Duration executorAwaitTermination = Duration.ofSeconds(30);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getDispatchFixedDelay() {
        return dispatchFixedDelay;
    }

    public void setDispatchFixedDelay(Duration dispatchFixedDelay) {
        this.dispatchFixedDelay = dispatchFixedDelay;
    }

    public Duration getSuccessReconcileFixedDelay() {
        return successReconcileFixedDelay;
    }

    public void setSuccessReconcileFixedDelay(Duration successReconcileFixedDelay) {
        this.successReconcileFixedDelay = successReconcileFixedDelay;
    }

    public Duration getFailureReconcileFixedDelay() {
        return failureReconcileFixedDelay;
    }

    public void setFailureReconcileFixedDelay(Duration failureReconcileFixedDelay) {
        this.failureReconcileFixedDelay = failureReconcileFixedDelay;
    }

    public Duration getEsCleanupFixedDelay() {
        return esCleanupFixedDelay;
    }

    public void setEsCleanupFixedDelay(Duration esCleanupFixedDelay) {
        this.esCleanupFixedDelay = esCleanupFixedDelay;
    }

    public Duration getFastgptCleanupFixedDelay() {
        return fastgptCleanupFixedDelay;
    }

    public void setFastgptCleanupFixedDelay(Duration fastgptCleanupFixedDelay) {
        this.fastgptCleanupFixedDelay = fastgptCleanupFixedDelay;
    }

    public Duration getDispatchLease() {
        return dispatchLease;
    }

    public void setDispatchLease(Duration dispatchLease) {
        this.dispatchLease = dispatchLease;
    }

    public Duration getSliceLease() {
        return sliceLease;
    }

    public void setSliceLease(Duration sliceLease) {
        this.sliceLease = sliceLease;
    }

    public Duration getCleanupLease() {
        return cleanupLease;
    }

    public void setCleanupLease(Duration cleanupLease) {
        this.cleanupLease = cleanupLease;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(Duration retryDelay) {
        this.retryDelay = retryDelay;
    }

    public int getClaimLimit() {
        return claimLimit;
    }

    public void setClaimLimit(int claimLimit) {
        this.claimLimit = claimLimit;
    }

    public int getExecutorCoreSize() {
        return executorCoreSize;
    }

    public void setExecutorCoreSize(int executorCoreSize) {
        this.executorCoreSize = executorCoreSize;
    }

    public int getExecutorMaxSize() {
        return executorMaxSize;
    }

    public void setExecutorMaxSize(int executorMaxSize) {
        this.executorMaxSize = executorMaxSize;
    }

    public int getExecutorQueueCapacity() {
        return executorQueueCapacity;
    }

    public void setExecutorQueueCapacity(int executorQueueCapacity) {
        this.executorQueueCapacity = executorQueueCapacity;
    }

    public Duration getExecutorAwaitTermination() {
        return executorAwaitTermination;
    }

    public void setExecutorAwaitTermination(Duration executorAwaitTermination) {
        this.executorAwaitTermination = executorAwaitTermination;
    }
}
