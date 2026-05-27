package com.thundax.kuzhambu.system.domain.core.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.system.domain.core.model.entity.Dict;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DictId;
import java.util.List;

public interface DictRepository {

    Dict getById(DictId id);

    List<Dict> listByIds(List<Long> idList);

    List<Dict> list(String type, String label, String remarks);

    PageResult<Dict> page(String type, String label, String remarks, int pageNo, int pageSize);

    int maxPriority();

    List<Dict> listByType(String type, SortDirection sortDirection);

    DictId insert(Dict dict);

    int update(Dict dict);

    int updatePriority(Dict dict);

    int deleteById(DictId id);

    List<String> listTypes();
}
