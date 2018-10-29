package cn.yanss.m.cookhouse.api.service;

import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.entity.ModifyOrderRequest;
import cn.yanss.m.entity.ReturnEntity;

public interface DispatcherService {

    ReturnEntity gainOrder();

    ReturnEntity cancelOrder(OrderResponse orderResponse);

    void orderCancel(ModifyOrderRequest orderResponse);

    void over(String orderId);

    void error(ModifyOrderRequest modifyOrderRequest);

    void anomaly(ModifyOrderRequest modifyOrderRequest);

    void complete(ModifyOrderRequest modifyOrderRequest);

    void pickup(ModifyOrderRequest modifyOrderRequest);

    void taskOrder(ModifyOrderRequest modifyOrderRequest);

    void haveOrder(ModifyOrderRequest modifyOrderRequest);

    ReturnEntity findRiderLocation(String orderId);

    void update(ModifyOrderRequest modifyOrderRequest);

    void flow(String orderId);

    ReturnEntity status(String orderId);
}
