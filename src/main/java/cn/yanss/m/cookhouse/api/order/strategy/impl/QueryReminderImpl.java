package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.cache.EhCacheServiceImpl;
import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.entity.OrderRequest;
import cn.yanss.m.cookhouse.api.order.strategy.QueryOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class QueryReminderImpl extends QueryOrder {

    @Autowired
    public QueryReminderImpl(EhCacheServiceImpl ehCacheService, RedisService redisService) {
        super(ehCacheService, redisService);
    }

    @Override
    public List<Object> list(Object obj) {
        OrderRequest orderRequest = (OrderRequest) obj;
        Integer start = (orderRequest.getCurrentPage() - 1) * orderRequest.getPageSize();
        Integer end = orderRequest.getCurrentPage() * orderRequest.getPageSize() - 1;
        Set<String> orders = redisService.zrange(Const.REMINDER + orderRequest.getStoreId(), start, end);
        return ehCacheService.getList(orders.stream().collect(Collectors.toList()));
    }

    @Override
    public Integer num() {

        return null;
    }

}
