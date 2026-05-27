package com.thundax.kuzhambu.system.infra.core.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.thundax.kuzhambu.common.cache.CacheDTO;
import com.thundax.kuzhambu.common.cache.KuzhambuCacheNames;
import com.thundax.kuzhambu.system.application.core.entity.Dict;
import com.thundax.kuzhambu.system.domain.core.codec.DictIdCodec;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class DictCacheSupport {

    private static final int OBJECT_EXPIRE_SECONDS = 3600;
    private static final String CACHE_SECTION = KuzhambuCacheNames.PREFIX + "sys.dict.";

    @CreateCache(
            name = CACHE_SECTION,
            cacheType = CacheType.BOTH,
            expire = OBJECT_EXPIRE_SECONDS,
            timeUnit = TimeUnit.SECONDS)
    private Cache<Long, DictCacheDTO> cache;

    public Optional<Dict> getById(Long id) {
        return toDomain(cache.get(id));
    }

    public void putById(Dict dict) {
        if (dict != null && DictIdCodec.toValue(dict.getId()) != null) {
            cache.put(DictIdCodec.toValue(dict.getId()), toCacheDTO(dict), OBJECT_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
    }

    public void removeById(Long id) {
        cache.remove(id);
    }

    private static Optional<Dict> toDomain(DictCacheDTO cacheDTO) {
        if (cacheDTO == null) {
            return Optional.empty();
        }
        Dict dict = new Dict();
        dict.setId(DictIdCodec.toDomain(cacheDTO.id));
        dict.setType(cacheDTO.type);
        dict.setLabel(cacheDTO.label);
        dict.setValue(cacheDTO.value);
        dict.setPriority(cacheDTO.priority == null ? 0 : cacheDTO.priority);
        dict.setRemarks(cacheDTO.remarks);
        return Optional.of(dict);
    }

    private static DictCacheDTO toCacheDTO(Dict dict) {
        DictCacheDTO cacheDTO = new DictCacheDTO();
        cacheDTO.id = DictIdCodec.toValue(dict.getId());
        cacheDTO.type = dict.getType();
        cacheDTO.label = dict.getLabel();
        cacheDTO.value = dict.getValue();
        cacheDTO.priority = dict.getPriority();
        cacheDTO.remarks = dict.getRemarks();
        return cacheDTO;
    }

    private static class DictCacheDTO implements CacheDTO {
        private Long id;
        private String type;
        private String label;
        private String value;
        private Integer priority;
        private String remarks;
    }
}
