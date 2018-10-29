package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.disruptor.NotifyServiceImpl;
import cn.yanss.m.cookhouse.api.entity.ModifyOrderRequest;
import cn.yanss.m.cookhouse.api.feign.OrderClient;
import cn.yanss.m.cookhouse.api.job.DelayJob;
import cn.yanss.m.cookhouse.api.order.strategy.CancelOrder;
import cn.yanss.m.cookhouse.api.service.thread.OrderModifyTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * 取消呼单
 */
@Component
public class CancelCallImpl implements CancelOrder {

    @Autowired
    private DelayJob delayJob;
    @Autowired
    private ExecutorService executorService;
    @Autowired
    private OrderClient orderClient;
    @Autowired
    private NotifyServiceImpl notifyService;

    @Override
    public void cancel(OrderResponse orderResponse) {
        Boolean isSuccess = delayJob.removeJob(orderResponse.getOrderId());
        if (isSuccess) {
            /**
             * 订单开始配送
             */
            orderResponse.setTotalStatus(Const.SIX);
            orderResponse.setOrderStatus(Const.THREE);
            orderResponse.setSendStatus(Const.FIVE);
            orderResponse.setSendType(Const.THREE);
            ModifyOrderRequest modifyOrderRequest = new ModifyOrderRequest();
            modifyOrderRequest.setOrderId(orderResponse.getOrderId());
            modifyOrderRequest.setSendStatus(Const.FIVE);
            modifyOrderRequest.setOrderStatus(Const.THREE);
            modifyOrderRequest.setDeliveryId("自配送");
            executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
            notifyService.sendNotify(orderResponse);
        }
    }
}
