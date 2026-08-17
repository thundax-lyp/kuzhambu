package com.thundax.kuzhambu.classics.facade;

import com.thundax.kuzhambu.classics.facade.request.ClassicsCleanupTargetsFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.KnowledgeGraphMaterialPageFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.KnowledgeGraphMaterialSnapshotFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsCleanupExecutionFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsCleanupTargetsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.KnowledgeGraphMaterialPageFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.KnowledgeGraphMaterialSnapshotFacadeResponse;

public interface ClassicsFacade {

    ClassicsSummaryFacadeResponse summary(ClassicsSummaryFacadeRequest request);

    ClassicsPublicContentsFacadeResponse listPublicContents();

    ClassicsPublicContentFacadeResponse getPublicContent(ClassicsPublicContentFacadeRequest request);

    ClassicsPublicContentsFacadeResponse listWorkbenchCategoryContents();

    ClassicsPublicContentsFacadeResponse listWorkbenchVolumeContents();

    ClassicsPublicContentsFacadeResponse listWorkbenchContents();

    ClassicsPublicContentsFacadeResponse listWorkbenchContents(String categoryCode, String volumeCode);

    ClassicsPublicContentFacadeResponse getWorkbenchContent(ClassicsPublicContentFacadeRequest request);

    ClassicsQaKnowledgeFacadeResponse getQaKnowledge(ClassicsQaKnowledgeFacadeRequest request);

    ClassicsQaKnowledgeFacadeResponse getWorkbenchQaKnowledge(ClassicsQaKnowledgeFacadeRequest request);

    ClassicsCleanupTargetsFacadeResponse listCleanupTargets(ClassicsCleanupTargetsFacadeRequest request);

    ClassicsCleanupExecutionFacadeResponse executeCleanupTargets(ClassicsCleanupTargetsFacadeRequest request);

    KnowledgeGraphMaterialPageFacadeResponse pageKnowledgeGraphMaterials(
            KnowledgeGraphMaterialPageFacadeRequest request);

    KnowledgeGraphMaterialSnapshotFacadeResponse getKnowledgeGraphMaterialSnapshot(
            KnowledgeGraphMaterialSnapshotFacadeRequest request);
}
