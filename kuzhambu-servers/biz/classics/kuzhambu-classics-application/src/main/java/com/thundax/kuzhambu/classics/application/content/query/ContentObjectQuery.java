package com.thundax.kuzhambu.classics.application.content.query;

import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;

public record ContentObjectQuery(String contentType, ClassicsContentId contentId) {}
