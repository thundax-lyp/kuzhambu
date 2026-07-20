package com.thundax.kuzhambu.classics.application.mingcustoms.command;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MingCustomsKeywordSortCommand {

    private List<MingCustomsKeywordId> orderedIds;
}
