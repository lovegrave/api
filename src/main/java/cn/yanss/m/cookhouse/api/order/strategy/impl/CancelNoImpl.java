package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.order.strategy.CancelOrder;
import cn.yanss.m.util.LogUtils;
import org.springframework.stereotype.Component;

/**
 * 取消配送无操作,空对象模式,防止空指针
 * @author HL
 */
@Component
public class CancelNoImpl implements CancelOrder {

    @Override
    public void cancel(OrderResponse orderResponse) {
        LogUtils.create().methodName("cancel").addValue(Const.ORDER_ID,orderResponse.getOrderId()).message("无取消操作").info();
    }
}
