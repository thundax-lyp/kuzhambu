package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.core.entity.Dict;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.DictId;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeDictInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateDictCommand;
import com.thundax.kuzhambu.system.application.core.service.command.DictSortCommand;
import com.thundax.kuzhambu.system.application.core.service.query.DictQuery;
import java.util.List;

public interface DictService {

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
