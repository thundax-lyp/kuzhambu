package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ClassicsPublicationBatchActionRequest(@NotEmpty List<@Valid @NotNull Long> ids) {}
