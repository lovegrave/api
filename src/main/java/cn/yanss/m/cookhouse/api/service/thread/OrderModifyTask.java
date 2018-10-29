package cn.yanss.m.cookhouse.api.service.thread;

import cn.yanss.m.cookhouse.api.entity.ModifyOrderRequest;
import cn.yanss.m.cookhouse.api.feign.OrderClient;
import cn.yanss.m.entity.ReturnEntity;
import cn.yanss.m.util.LogUtils;

import java.util.concurrent.Callable;


/**
 * @author
 */
public class OrderModifyTask implements Callable<ReturnEntity> {
    private OrderClient orderClient;
    private ModifyOrderRequest modifyOrderRequest;

    public OrderModifyTask(ModifyOrderRequest modifyOrderRequest, OrderClient orderClient) {
        this.modifyOrderRequest = modifyOrderRequest;
        this.orderClient = orderClient;
    }

    @Override
    public ReturnEntity call() {
        try {
            LogUtils.create().methodName("OrderModifyTask").key(modifyOrderRequest.getOrderId()).message("订单修改调度").info();
            return orderClient.modifyOrderStatus(modifyOrderRequest);
        } catch (Exception e) {
            LogUtils.create().methodName("OrderModifyTask").key(modifyOrderRequest.getOrderId()).message(e.getMessage()).error();
            return new ReturnEntity();
        }
    }
}
