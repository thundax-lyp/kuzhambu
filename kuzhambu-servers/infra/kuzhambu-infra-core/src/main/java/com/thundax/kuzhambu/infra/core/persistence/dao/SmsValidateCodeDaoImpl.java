package com.thundax.kuzhambu.infra.core.persistence.dao;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.thundax.kuzhambu.biz.core.dao.SmsValidateCodeDao;
import com.thundax.kuzhambu.common.cache.KuzhambuCacheNames;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Repository;

@Repository
public class SmsValidateCodeDaoImpl implements SmsValidateCodeDao {

    private static final String CACHE_MOBILE = KuzhambuCacheNames.PREFIX + "smsValidateMobile.";

    @CreateCache(name = CACHE_MOBILE, cacheType = CacheType.BOTH)
    private Cache<String, String> cache;

    @Override
    public boolean canSend(String mobile) {
        return cache.get(mobile) == null;
    }

    @Override
    public void markSent(String mobile, int expiredSeconds) {
        cache.put(mobile, "1", expiredSeconds, TimeUnit.SECONDS);
    }
}
