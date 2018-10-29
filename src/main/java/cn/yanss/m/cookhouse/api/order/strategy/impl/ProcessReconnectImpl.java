package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.order.strategy.ProcessOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 取消配送的订单异常,再次进行接单,发起配送
 * @author HL
 */
@Component
public class ProcessReconnectImpl implements ProcessOrder {
    @Autowired
    private RedisService redisService;
    @Override
    public void process(OrderResponse orderResponse) {
        String orderId = orderResponse.getOrderId();
        Long len = redisService.zrem(Const.ORDER_STATUS + orderResponse.getStoreId() + Const.NINE, orderId);
        if (len == 1) {
            orderResponse.setSendStatus(Const.TWO);
            orderResponse.setTotalStatus(Const.THREE);
            orderResponse.setExceptionStatus(0);
            if (orderResponse.getDeliveryType() == Const.ONE) {
                redisService.sadd(Const.FLOW, 3600, orderId);
            }
        }
    }
}
