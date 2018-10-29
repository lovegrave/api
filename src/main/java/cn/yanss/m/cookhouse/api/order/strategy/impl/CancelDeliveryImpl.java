package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.order.strategy.CancelOrder;
import cn.yanss.m.cookhouse.api.service.DispatcherService;
import cn.yanss.m.entity.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 确认退款后,如果未取消配送,则确认取消配送
 * @author HL
 */
@Component
public class CancelDeliveryImpl implements CancelOrder {

    @Autowired
    private DispatcherService dispatcherService;

    @Override
    public void cancel(OrderResponse orderResponse) {
        int sendStatus = orderResponse.getSendStatus();
        if(sendStatus < 3 || sendStatus > 5){
            return;
        }
        orderResponse.setCancelCode(1);
        ReturnEntity entity = dispatcherService.cancelOrder(orderResponse);
        orderResponse.setTotalStatus(98);
        if (entity.getCode() == 200) {
            orderResponse.setSendStatus(99);
            orderResponse.setExceptionStatus(0);
        } else {
            orderResponse.setExceptionStatus(99);
            orderResponse.setExceptionRemark(entity.getMsg());
        }
    }
}
