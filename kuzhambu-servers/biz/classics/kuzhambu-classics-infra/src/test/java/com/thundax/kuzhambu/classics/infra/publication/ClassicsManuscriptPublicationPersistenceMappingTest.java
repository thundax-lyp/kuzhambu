package com.thundax.kuzhambu.classics.infra.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.publication.codec.ClassicsPublicationJobIdCodec;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.assembler.MingCustomsPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.assembler.SancaiPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.assembler.WangqiDocumentPersistenceAssembler;
import org.junit.jupiter.api.Test;

class ClassicsManuscriptPublicationPersistenceMappingTest {

    @Test
    void shouldRoundTripSancaiPublicationState() {
        SancaiEntry source = new SancaiEntry();
        source.setLifecycleStatus(SancaiEntryLifecycleStatus.PUBLISHED);
        source.setTransitionStatus(ClassicsPublicationTransitionStatus.OFFLINING);
        source.setCurrentPublicationJobId(ClassicsPublicationJobIdCodec.toDomain(101L));

        SancaiEntry restored = SancaiPersistenceAssembler.toDomain(SancaiPersistenceAssembler.toObject(source));

        assertEquals(source.getLifecycleStatus(), restored.getLifecycleStatus());
        assertEquals(source.getTransitionStatus(), restored.getTransitionStatus());
        assertEquals(source.getCurrentPublicationJobId(), restored.getCurrentPublicationJobId());
    }

    @Test
    void shouldRoundTripWangqiPublicationState() {
        WangqiDocument source = new WangqiDocument();
        source.setLifecycleStatus(ClassicsPublicationLifecycleStatus.ERROR);
        source.setTransitionStatus(ClassicsPublicationTransitionStatus.PUBLISHING);
        source.setCurrentPublicationJobId(ClassicsPublicationJobIdCodec.toDomain(102L));

        WangqiDocument restored =
                WangqiDocumentPersistenceAssembler.toDomain(WangqiDocumentPersistenceAssembler.toObject(source));

        assertEquals(source.getLifecycleStatus(), restored.getLifecycleStatus());
        assertEquals(source.getTransitionStatus(), restored.getTransitionStatus());
        assertEquals(source.getCurrentPublicationJobId(), restored.getCurrentPublicationJobId());
    }

    @Test
    void shouldRoundTripMingCustomsPublicationState() {
        MingCustomsEntry source = new MingCustomsEntry();
        source.setLifecycleStatus(ClassicsPublicationLifecycleStatus.OFFLINE);
        source.setTransitionStatus(ClassicsPublicationTransitionStatus.NONE);
        source.setCurrentPublicationJobId(ClassicsPublicationJobIdCodec.toDomain(103L));

        MingCustomsEntry restored =
                MingCustomsPersistenceAssembler.toDomain(MingCustomsPersistenceAssembler.toObject(source));

        assertEquals(source.getLifecycleStatus(), restored.getLifecycleStatus());
        assertEquals(source.getTransitionStatus(), restored.getTransitionStatus());
        assertEquals(source.getCurrentPublicationJobId(), restored.getCurrentPublicationJobId());
    }
}
