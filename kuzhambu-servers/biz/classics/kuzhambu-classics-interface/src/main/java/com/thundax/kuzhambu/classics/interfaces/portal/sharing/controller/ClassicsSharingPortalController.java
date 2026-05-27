package com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller;

import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/portal/classics/shares")
@WrappedApiController
public class ClassicsSharingPortalController {
    private final ClassicsSharingApplicationService service;
    public ClassicsSharingPortalController(ClassicsSharingApplicationService service) { this.service = service; }
    @GetMapping("{tokenHash}/targets") public List<ClassicsShareTarget> targets(@PathVariable String tokenHash) { return service.listTargets(service.getLinkByTokenHash(tokenHash).getId()); }
}
