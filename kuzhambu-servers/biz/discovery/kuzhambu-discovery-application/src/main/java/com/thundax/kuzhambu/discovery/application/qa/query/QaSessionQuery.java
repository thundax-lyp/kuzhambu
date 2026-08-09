package com.thundax.kuzhambu.discovery.application.qa.query;

import java.time.Instant;

public record QaSessionQuery(String title, Instant openedAtStart, Instant openedAtEnd) {}
