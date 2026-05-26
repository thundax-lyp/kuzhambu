package com.thundax.kuzhambu.biz.core.service;

import com.thundax.kuzhambu.biz.core.entity.Dict;
import com.thundax.kuzhambu.biz.core.entity.valueobject.DictId;
import com.thundax.kuzhambu.biz.core.service.command.ChangeDictInfoCommand;
import com.thundax.kuzhambu.biz.core.service.command.CreateDictCommand;
import com.thundax.kuzhambu.biz.core.service.command.DictSortCommand;
import com.thundax.kuzhambu.biz.core.service.query.DictQuery;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
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
