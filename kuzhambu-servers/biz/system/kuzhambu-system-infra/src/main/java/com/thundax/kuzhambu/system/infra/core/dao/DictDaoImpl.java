package com.thundax.kuzhambu.system.infra.core.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.system.application.core.dao.DictDao;
import com.thundax.kuzhambu.system.application.core.entity.Dict;
import com.thundax.kuzhambu.system.domain.core.codec.DictIdCodec;
import com.thundax.kuzhambu.system.domain.model.valueobject.DictId;
import com.thundax.kuzhambu.system.infra.core.assembler.DictPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.core.cache.DictCacheSupport;
import com.thundax.kuzhambu.system.infra.core.dataobject.DictDO;
import com.thundax.kuzhambu.system.infra.core.mapper.DictMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class DictDaoImpl implements DictDao {

    private final DictMapper mapper;
    private final DictCacheSupport cacheSupport;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public DictDaoImpl(DictMapper mapper, DictCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Dict getById(DictId id) {
        Optional<Dict> cachedDict = cacheSupport.getById(id.value());
        if (cachedDict.isPresent()) {
            return cachedDict.get();
        }

        Dict dict = DictPersistenceAssembler.toEntity(mapper.selectById(id.value()));
        cacheSupport.putById(dict);
        return dict;
    }

    @Override
    public List<Dict> listByIds(List<Long> idList) {
        List<Dict> dictList = new ArrayList<>();
        List<Long> uncachedIdList = new ArrayList<>();
        for (Long id : idList) {
            Optional<Dict> cachedDict = cacheSupport.getById(id);
            cachedDict.ifPresent(dictList::add);
            if (!cachedDict.isPresent()) {
                uncachedIdList.add(id);
            }
        }

        if (!uncachedIdList.isEmpty()) {
            List<Dict> uncachedDictList = DictPersistenceAssembler.toEntityList(mapper.selectBatchIds(uncachedIdList));
            for (Dict dict : uncachedDictList) {
                cacheSupport.putById(dict);
                dictList.add(dict);
            }
        }
        return dictList;
    }

    @Override
    public List<Dict> list(String type, String label, String remarks) {
        return DictPersistenceAssembler.toEntityList(mapper.selectList(buildQueryWrapper(type, label, remarks)));
    }

    @Override
    public Page<Dict> page(String type, String label, String remarks, int pageNo, int pageSize) {
        Page<DictDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildQueryWrapper(type, label, remarks));
        Page<Dict> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(DictPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public int maxPriority() {
        QueryWrapper<DictDO> wrapper = new QueryWrapper<>();
        Object max = mapper.selectObjs(wrapper.select("max(priority)")).stream()
                .findFirst()
                .orElse(null);
        if (max == null) {
            return 0;
        }
        if (max instanceof Number) {
            return ((Number) max).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(max));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    @Override
    public List<Dict> listByType(String type, SortDirection sortDirection) {
        return DictPersistenceAssembler.toEntityList(mapper.selectList(buildListByTypeWrapper(type, sortDirection)));
    }

    @Override
    public DictId insert(Dict entity) {
        DictDO dataObject = DictPersistenceAssembler.toDataObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        cacheSupport.putById(DictPersistenceAssembler.toEntity(dataObject));
        return DictIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(Dict entity) {
        DictDO dataObject = DictPersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(DictDO::getValue, dataObject.getValue())
                        .set(DictDO::getLabel, dataObject.getLabel())
                        .set(DictDO::getType, dataObject.getType())
                        .set(DictDO::getRemarks, dataObject.getRemarks()));
        if (count > 0) {
            cacheSupport.removeById(dataObject.getId());
        }
        return count;
    }

    @Override
    public int updatePriority(Dict entity) {
        DictDO dataObject = DictPersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(DictDO::getPriority, dataObject.getPriority()));
        if (count > 0) {
            cacheSupport.removeById(dataObject.getId());
        }
        return count;
    }

    public int updateStatus(Dict entity) {
        return 0;
    }

    @Override
    public int deleteById(DictId id) {
        int count = mapper.deleteById(id.value());
        if (count > 0) {
            cacheSupport.removeById(id.value());
        }
        return count;
    }

    @Override
    public List<String> listTypes() {
        QueryWrapper<DictDO> wrapper = new QueryWrapper<>();
        wrapper.select("type").groupBy("type").orderByAsc("type");
        return mapper.selectObjs(wrapper).stream().map(String::valueOf).collect(Collectors.toList());
    }

    private QueryWrapper<DictDO> buildQueryWrapper(String type, String label, String remarks) {
        QueryWrapper<DictDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(type)) {
            wrapper.eq("type", type);
        }
        if (StringUtils.isNotBlank(label)) {
            wrapper.like("label", label);
        }
        if (StringUtils.isNotBlank(remarks)) {
            wrapper.like("remarks", remarks);
        }
        wrapper.orderByAsc("type", "priority", "id");
        return wrapper;
    }

    private QueryWrapper<DictDO> buildListByTypeWrapper(String type, SortDirection sortDirection) {
        QueryWrapper<DictDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(type)) {
            wrapper.eq("type", type);
        }
        if (SortDirection.DESC == sortDirection) {
            wrapper.orderByDesc("priority");
        } else {
            wrapper.orderByAsc("priority");
        }
        wrapper.orderByAsc("id");
        return wrapper;
    }

    private LambdaUpdateWrapper<DictDO> buildIdUpdateWrapper(DictDO dataObject) {
        LambdaUpdateWrapper<DictDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DictDO::getId, dataObject.getId());
        return wrapper;
    }
}
