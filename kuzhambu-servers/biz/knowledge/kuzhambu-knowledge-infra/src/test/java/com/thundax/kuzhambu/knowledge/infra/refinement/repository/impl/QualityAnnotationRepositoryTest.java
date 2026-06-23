package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityAnnotation;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityAnnotationDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.QualityAnnotationMapper;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class QualityAnnotationRepositoryTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listBySourceShouldQueryObjectAndSourceScope() {
        QualityAnnotationMapper mapper = mock(QualityAnnotationMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        QualityAnnotationRepositoryImpl repository = new QualityAnnotationRepositoryImpl(mapper);

        repository.listBySource("ENTITY", "SANCAI_ENTRY", 100L, 11L);

        ArgumentCaptor<QueryWrapper<QualityAnnotationDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectList(captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("object_type"));
        assertTrue(sqlSegment.contains("source_content_type"));
        assertTrue(sqlSegment.contains("graph_version_id"));
    }

    @Test
    void saveOrUpdateShouldInsertWhenAnnotationDoesNotExist() {
        QualityAnnotationMapper mapper = mock(QualityAnnotationMapper.class);
        when(mapper.update(any(), any())).thenReturn(0);
        when(mapper.insert(any(QualityAnnotationDO.class))).thenReturn(1);
        QualityAnnotationRepositoryImpl repository = new QualityAnnotationRepositoryImpl(mapper);
        QualityAnnotation annotation = new QualityAnnotation(
                null,
                null,
                "ENTITY",
                "entity:a",
                "SANCAI_ENTRY",
                100L,
                11L,
                "OPEN",
                "CORRECT",
                "说明",
                1L,
                new Date(),
                1L,
                new Date());

        repository.saveOrUpdate(annotation);

        verify(mapper).insert(any(QualityAnnotationDO.class));
    }
}
