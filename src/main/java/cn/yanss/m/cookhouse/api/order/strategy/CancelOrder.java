package cn.yanss.m.cookhouse.api.order.strategy;

import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;

/**
 * 取消配送
 * @author HL
 */
public interface CancelOrder {

    void cancel(OrderResponse orderResponse);
}
