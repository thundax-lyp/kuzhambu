package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphMaterialMapper;

abstract class GraphRepositorySupport {
    private final GraphMaterialMapper materialMapper;

    GraphRepositorySupport(GraphMaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    protected Long materialId(ContentRef materialRef) {
        QueryWrapper<GraphMaterialDO> wrapper = new QueryWrapper<>();
        wrapper.select("id")
                .eq("content_type", ContentRefCodec.toContentType(materialRef))
                .eq("content_ref_id", ContentRefCodec.toValue(materialRef));
        GraphMaterialDO material = materialMapper.selectOne(wrapper);
        return material == null ? null : material.getId();
    }
}
