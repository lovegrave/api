package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.RefundDao;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.feign.ManagerClient;
import cn.yanss.m.cookhouse.api.order.strategy.RefundOrder;
import cn.yanss.m.util.LogUtils;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 全部退款的操作
 * @author HL
 */
@Component
public class RefundFullImpl implements RefundOrder {

    @Autowired
    private ManagerClient managerClient;
    @Autowired
    private RefundDao refundDao;
    @Autowired
    private RedisService redisService;
    @Override
    public void refund(OrderResponse orderResponse, JSONArray array) {
        LogUtils.create().methodName("fullRefund").type(Const.REFUND).key(orderResponse.getOrderId()).info();
        Map<String, Object> map = new HashMap<>(3);
        map.put("payPrice", orderResponse.getPayPrice());
        map.put(Const.ORDER_ID, orderResponse.getOrderId());
        int refundPrice= orderResponse.getPayPrice()-orderResponse.getRefundResponse().getRefundedPrice();
        map.put(Const.PRICE, refundPrice);
        int code = managerClient.refund(map).getCode();
        redisService.zrem(Const.REFUND+orderResponse.getStoreId()+0,orderResponse.getOrderId());
        redisService.zrem(Const.REFUND+orderResponse.getStoreId()+1,orderResponse.getOrderId());
        orderResponse.setTotalStatus(98);
        if(200 == code){
            orderResponse.setRefundStatus(18);
            orderResponse.getRefundResponse().setRefundedPrice(orderResponse.getPayPrice());
            orderResponse.getRefundResponse().setApplyRefundPrice(0);
            orderResponse.getRefundResponse().setRefundFailurePrice(0);
            redisService.zadd(Const.REFUND + orderResponse.getStoreId() + 1, System.currentTimeMillis() * 0.001, orderResponse.getOrderId(), 28800);
        }else{
            orderResponse.setRefundStatus(19);
            orderResponse.getRefundResponse().setRefundFailurePrice(orderResponse.getPayPrice());
            redisService.zadd(Const.REFUND + orderResponse.getStoreId() + 0, System.currentTimeMillis() * 0.001, orderResponse.getOrderId(), 28800);
        }
        refundDao.insertApply(orderResponse,refundPrice, "");
    }
}
