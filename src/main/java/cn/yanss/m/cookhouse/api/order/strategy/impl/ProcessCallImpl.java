package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.job.DelayJob;
import cn.yanss.m.cookhouse.api.order.strategy.ProcessOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 立即呼单流程操作
 * @author HL
 */
@Component
public class ProcessCallImpl implements ProcessOrder {
    @Autowired
    private DelayJob delayJob;
    @Override
    public void process(OrderResponse orderResponse) {
        String orderId = orderResponse.getOrderId();
        delayJob.removeJob(orderId);
        delayJob.createDelayFlowJob(orderId);
    }
}
