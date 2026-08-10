package com.thundax.kuzhambu.classics.application.search.service;

import com.thundax.kuzhambu.classics.application.search.query.ClassicsSearchContentQuery;
import com.thundax.kuzhambu.classics.application.search.query.ClassicsWorkbenchContentQuery;
import com.thundax.kuzhambu.classics.application.search.result.ClassicsSearchSourceContent;
import java.util.List;

public interface ClassicsSearchContentApplicationService {

    List<ClassicsSearchSourceContent> listPublicContents();

    ClassicsSearchSourceContent getPublicContent(ClassicsSearchContentQuery query);

    List<ClassicsSearchSourceContent> listWorkbenchCategoryContents();

    List<ClassicsSearchSourceContent> listWorkbenchVolumeContents();

    List<ClassicsSearchSourceContent> listWorkbenchContents();

    List<ClassicsSearchSourceContent> listWorkbenchContents(ClassicsWorkbenchContentQuery query);

    ClassicsSearchSourceContent getWorkbenchContent(ClassicsSearchContentQuery query);
}
