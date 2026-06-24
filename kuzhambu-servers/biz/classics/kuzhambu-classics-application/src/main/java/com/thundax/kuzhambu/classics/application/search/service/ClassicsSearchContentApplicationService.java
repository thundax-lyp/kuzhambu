package com.thundax.kuzhambu.classics.application.search.service;

import com.thundax.kuzhambu.classics.application.search.result.ClassicsSearchSourceContent;
import java.util.List;

public interface ClassicsSearchContentApplicationService {

    List<ClassicsSearchSourceContent> listPublicContents();

    ClassicsSearchSourceContent getPublicContent(String contentType, String contentId);
}
