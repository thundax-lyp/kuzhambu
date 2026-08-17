package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import java.util.List;

public interface GraphMaterialRepository {
    GraphMaterial getByContentRef(ContentRef contentRef);

    List<GraphMaterial> listByContentRefs(List<ContentRef> contentRefs);

    List<ContentRef> listContentRefsByStatus(GraphMaterialStatus status);

    List<ContentRef> listContentRefsByStatuses(List<GraphMaterialStatus> statuses);

    PageResult<GraphMaterial> page(String keyword, GraphMaterialStatus status, int pageNo, int pageSize);

    int insert(GraphMaterial material);

    int update(GraphMaterial material);

    int updateIfLockVersion(GraphMaterial material, long expectedLockVersion);

    int deleteByContentRef(ContentRef contentRef);
}
