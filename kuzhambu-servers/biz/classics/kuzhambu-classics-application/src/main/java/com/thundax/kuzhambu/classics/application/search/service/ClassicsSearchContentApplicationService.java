package com.thundax.kuzhambu.classics.application.search.service;

import com.thundax.kuzhambu.classics.application.search.result.ClassicsSearchSourceContent;
import java.util.List;

public interface ClassicsSearchContentApplicationService {

    List<ClassicsSearchSourceContent> listPublicContents();

    ClassicsSearchSourceContent getPublicContent(String contentType, String contentId);

    List<ClassicsSearchSourceContent> listWorkbenchCategoryContents();

    List<ClassicsSearchSourceContent> listWorkbenchVolumeContents();

    List<ClassicsSearchSourceContent> listWorkbenchContents();

    List<ClassicsSearchSourceContent> listWorkbenchContents(String categoryCode, String volumeCode);

    ClassicsSearchSourceContent getWorkbenchContent(String contentType, String contentId);
}
