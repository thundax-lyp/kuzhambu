package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.command.ChangeDictInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateDictCommand;
import com.thundax.kuzhambu.system.application.core.command.DictSortCommand;
import com.thundax.kuzhambu.system.application.core.query.DictQuery;
import com.thundax.kuzhambu.system.domain.core.model.entity.Dict;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DictId;
import java.util.List;

public interface DictApplicationService {

    Dict get(DictId id);

    List<Dict> list(DictQuery query);

    PageResult<Dict> page(DictQuery query, PageQuery page);

    DictId create(CreateDictCommand command);

    void sort(DictSortCommand command);

    void changeInfo(ChangeDictInfoCommand command);

    void remove(DictId id);

    List<String> listTypes(DictQuery query);

    List<String> listLabels(DictQuery query);
}
