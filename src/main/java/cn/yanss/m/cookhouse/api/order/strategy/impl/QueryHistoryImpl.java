package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.cache.EhCacheServiceImpl;
import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.order.strategy.QueryOrder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QueryHistoryImpl extends QueryOrder {
    public QueryHistoryImpl(EhCacheServiceImpl ehCacheService, RedisService redisService) {
        super(ehCacheService, redisService);
    }

    @Override
    public List<Object> list(Object obj) {
        return null;
    }


    @Override
    public Integer num(){
        return 0;
    }


}
