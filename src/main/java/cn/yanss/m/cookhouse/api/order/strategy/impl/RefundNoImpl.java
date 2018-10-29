package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.order.strategy.RefundOrder;
import cn.yanss.m.util.LogUtils;
import com.alibaba.fastjson.JSONArray;
import org.springframework.stereotype.Component;

/**
 * 空的退款操作,空对象模式,防止空指针
 * @author HL
 */
@Component
public class RefundNoImpl implements RefundOrder {

    @Override
    public void refund(OrderResponse orderResponse, JSONArray array) {
        LogUtils.create().methodName("RefundNoImpl").message("无退款状态").addValue(Const.ORDER_ID,orderResponse.getOrderId()).info();
    }
}
