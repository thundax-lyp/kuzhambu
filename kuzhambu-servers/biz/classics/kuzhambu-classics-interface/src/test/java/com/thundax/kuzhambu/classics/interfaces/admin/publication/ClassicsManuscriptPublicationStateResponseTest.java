package com.thundax.kuzhambu.classics.interfaces.admin.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.assembler.MingCustomsInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler.SancaiInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.assembler.WangqiDocumentInterfaceAssembler;
import org.junit.jupiter.api.Test;

class ClassicsManuscriptPublicationStateResponseTest {

    @Test
    void shouldExposePublicationStateForAllManuscriptTypes() {
        SancaiEntry sancai = new SancaiEntry();
        sancai.setLifecycleStatus(SancaiEntryLifecycleStatus.DRAFT);
        sancai.setTransitionStatus(ClassicsPublicationTransitionStatus.PUBLISHING);
        sancai.setCurrentPublicationJobId(new ClassicsPublicationJobId(21L));

        WangqiDocument wangqi = new WangqiDocument();
        wangqi.setLifecycleStatus(ClassicsPublicationLifecycleStatus.PUBLISHED);
        wangqi.setTransitionStatus(ClassicsPublicationTransitionStatus.OFFLINING);
        wangqi.setCurrentPublicationJobId(new ClassicsPublicationJobId(22L));

        MingCustomsEntry ming = new MingCustomsEntry();
        ming.setLifecycleStatus(ClassicsPublicationLifecycleStatus.ERROR);
        ming.setTransitionStatus(ClassicsPublicationTransitionStatus.NONE);
        ming.setCurrentPublicationJobId(new ClassicsPublicationJobId(23L));

        var sancaiResponse = SancaiInterfaceAssembler.toResponse(sancai);
        var wangqiResponse = WangqiDocumentInterfaceAssembler.toResponse(wangqi);
        var mingResponse = MingCustomsInterfaceAssembler.toResponse(ming);

        assertEquals("DRAFT", sancaiResponse.getLifecycleStatus());
        assertEquals("PUBLISHING", sancaiResponse.getTransitionStatus());
        assertEquals(21L, sancaiResponse.getCurrentPublicationJobId());
        assertEquals("PUBLISHED", wangqiResponse.getLifecycleStatus());
        assertEquals("OFFLINING", wangqiResponse.getTransitionStatus());
        assertEquals(22L, wangqiResponse.getCurrentPublicationJobId());
        assertEquals("ERROR", mingResponse.getLifecycleStatus());
        assertEquals("NONE", mingResponse.getTransitionStatus());
        assertEquals(23L, mingResponse.getCurrentPublicationJobId());
    }
}
