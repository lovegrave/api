package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.entity.ModifyOrderRequest;
import cn.yanss.m.cookhouse.api.feign.OrderClient;
import cn.yanss.m.cookhouse.api.job.DelayJob;
import cn.yanss.m.cookhouse.api.order.strategy.ProcessOrder;
import cn.yanss.m.cookhouse.api.service.thread.OrderModifyTask;
import cn.yanss.m.exception.MallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

import static cn.yanss.m.exception.constant.ExceptionEnum.ORDER_PROCESSED;

/**
 * 正常的接单流程操作
 * @author HL
 */
@Component
public class ProcessReceiveImpl implements ProcessOrder {
    @Autowired
    private RedisService redisService;
    @Autowired
    private DelayJob delayJob;
    @Autowired
    private ExecutorService executorService;
    @Autowired
    private OrderClient orderClient;
    @Override
    public void process(OrderResponse orderResponse) throws MallException {
        String orderId = orderResponse.getOrderId();
        Long l = redisService.zrem(Const.ORDER_STATUS + orderResponse.getStoreId() + 1, orderResponse.getOrderId());
        /**
         * 如果删除订单的流程状态失败,则接单失败
         */
        if (l != Const.ONE || !delayJob.removeJob(orderId)) {
            throw new MallException(ORDER_PROCESSED);
        }
        ModifyOrderRequest modifyOrderRequest = new ModifyOrderRequest();
        modifyOrderRequest.setOrderId(orderId);
        modifyOrderRequest.setOrderStatus(Const.TWO);
        modifyOrderRequest.setSendStatus(Const.TWO);
        executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
        orderResponse.setTotalStatus(Const.TWO);
        orderResponse.setOrderStatus(Const.TWO);
        orderResponse.setSendStatus(Const.TWO);
        /**
         * 如果是配送的订单，添加呼单延迟任务
         */
        if (orderResponse.getDeliveryType() == Const.ONE) {
            delayJob.createDelayFlowJob(orderId);
        }
    }
}
