package cn.yanss.m.cookhouse.api.order.strategy;

import cn.yanss.m.cookhouse.api.cache.EhCacheServiceImpl;
import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;

import java.util.List;

public abstract class QueryOrder {

    protected EhCacheServiceImpl ehCacheService;
    protected RedisService redisService;

    public QueryOrder(EhCacheServiceImpl ehCacheService, RedisService redisService) {
        this.ehCacheService = ehCacheService;
        this.redisService = redisService;
    }

    public abstract List<Object> list(Object obj);


    public OrderResponse order(String orderId){
        OrderResponse orderResponse = (OrderResponse) ehCacheService.getObj(orderId);
        if(null == orderResponse){
            orderResponse = redisService.getObject(orderId,OrderResponse.class);
        }
        return orderResponse;
    }

    public abstract Integer num();
}
