package com.thundax.kuzhambu.classics.application.sancai.query;

import java.time.Instant;

public record SancaiShowcaseQuery(
        String keyword, String status, String visibilityRiskStatus, Instant requestedAtStart, Instant requestedAtEnd) {}
