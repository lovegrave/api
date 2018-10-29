package cn.yanss.m.cookhouse.api.order.strategy;

import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.exception.MallException;

/**
 * 流程操作
 * @author HL
 */
public interface ProcessOrder {

    void process(OrderResponse orderResponse) throws MallException;
}
