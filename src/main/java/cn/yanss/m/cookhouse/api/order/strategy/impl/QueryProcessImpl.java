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
public class QueryProcessImpl extends QueryOrder {

    @Autowired
    public QueryProcessImpl(EhCacheServiceImpl ehCacheService, RedisService redisService) {
        super(ehCacheService, redisService);
    }

    @Override
    public List<Object> list(Object obj) {
        OrderRequest orderRequest = (OrderRequest) obj;
        Integer start = (orderRequest.getCurrentPage() - 1) * orderRequest.getPageSize();
        Integer end = orderRequest.getCurrentPage() * orderRequest.getPageSize() - 1;
        String key = Const.ORDER_STATUS + orderRequest.getStoreId() + orderRequest.getTotalStatus();
        Set<String> keys = redisService.zrange(key, start, end);
        List<String> orderIds = keys.stream().collect(Collectors.toList());
        return ehCacheService.getList(orderIds);
    }

    @Override
    public Integer num() {
        return null;
    }

}
