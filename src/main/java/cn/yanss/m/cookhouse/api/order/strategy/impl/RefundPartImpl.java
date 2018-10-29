package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.RefundDao;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.dao.entity.ProductResponse;
import cn.yanss.m.cookhouse.api.feign.ManagerClient;
import cn.yanss.m.cookhouse.api.order.strategy.RefundOrder;
import cn.yanss.m.util.LogUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部分退款操作
 * @author HL
 */
@Component
public class RefundPartImpl implements RefundOrder {

    @Autowired
    private ManagerClient managerClient;
    @Autowired
    private RefundDao refundDao;
    @Autowired
    private RedisService redisService;

    @Override
    public void refund(OrderResponse orderResponse, JSONArray product) {

        LogUtils.create().methodName("PartialRefund").type(Const.REFUND).key(orderResponse.getOrderId()).info();
        Map<String, Object> map = new HashMap<>(3);
        int payPrice = orderResponse.getPayPrice();
        map.put("payPrice", payPrice);
        map.put(Const.ORDER_ID, orderResponse.getOrderId());
        int price = orderResponse.getRefundResponse().getApplyRefundPrice();
        int refundedPrice = orderResponse.getRefundResponse().getRefundedPrice();
        /**
         * 判断退款总金额是否多余订单总金额
         */
        if(payPrice - refundedPrice < price){
            price = payPrice - refundedPrice;
        }
        map.put(Const.PRICE, price);
        int code = managerClient.refund(map).getCode();
        redisService.zrem(Const.REFUND+orderResponse.getStoreId()+0,orderResponse.getOrderId());
        redisService.zrem(Const.REFUND+orderResponse.getStoreId()+1,orderResponse.getOrderId());
        if(code == 200){
            orderResponse.setRefundStatus(28);
            orderResponse.getRefundResponse().setRefundedPrice(price+orderResponse.getRefundResponse().getRefundedPrice());
            orderResponse.getRefundResponse().setApplyRefundPrice(0);
            orderResponse.getRefundResponse().setRefundFailurePrice(0);
            redisService.zadd(Const.REFUND + orderResponse.getStoreId() + 1, System.currentTimeMillis() * 0.001, orderResponse.getOrderId(), 28800);
        }else{
            orderResponse.setRefundStatus(29);
            orderResponse.getRefundResponse().setRefundFailurePrice(price);
            redisService.zadd(Const.REFUND + orderResponse.getStoreId() + 0, System.currentTimeMillis() * 0.001, orderResponse.getOrderId(), 28800);
        }
        StringBuilder pid = new StringBuilder();
        if (!product.isEmpty()){
            int len = product.size();
            List<ProductResponse> productResponse = orderResponse.getCartInfo().getListProduct();
            for (int i = 0; i < len; i++) {
                JSONObject object = product.getJSONObject(i);
                String productId = object.getString("productId");
                pid.append(productId);
                pid.append(",");
                for (ProductResponse p : productResponse) {
                    JSONArray array = null == p.getRefundRecord()?new JSONArray():p.getRefundRecord();
                    if (p.getProductId().equalsIgnoreCase(productId)) {
                        object.remove("productId");
                        object.put(Const.REFUND_STATUS, code == 200?18:19);
                        array.add(object);
                        p.setRefundRecord(array);
                    }
                }
            }
        }
        refundDao.insertApply(orderResponse, price,pid.toString());
    }
}
