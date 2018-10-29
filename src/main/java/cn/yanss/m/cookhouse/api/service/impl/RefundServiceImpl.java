package cn.yanss.m.cookhouse.api.service.impl;

import cn.yanss.m.cookhouse.api.cache.EhCacheServiceImpl;
import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.dao.entity.RefundResponse;
import cn.yanss.m.cookhouse.api.disruptor.NotifyServiceImpl;
import cn.yanss.m.cookhouse.api.entity.ModifyOrderRequest;
import cn.yanss.m.cookhouse.api.factory.OrderFactory;
import cn.yanss.m.cookhouse.api.feign.OrderClient;
import cn.yanss.m.cookhouse.api.job.DelayJob;
import cn.yanss.m.cookhouse.api.service.RefundService;
import cn.yanss.m.cookhouse.api.service.thread.OrderModifyTask;
import cn.yanss.m.cookhouse.api.utils.MapperUtils;
import cn.yanss.m.entity.ReturnEntity;
import cn.yanss.m.util.LogUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * 退款业务
 */
@Service
public class RefundServiceImpl implements RefundService {

    @Autowired
    private RedisService redisService;
    @Autowired
    private EhCacheServiceImpl ehCacheService;
    @Autowired
    private DelayJob delayJob;
    @Autowired
    private OrderClient orderClient;
    @Autowired
    private NotifyServiceImpl notifyService;
    @Autowired
    private ExecutorService executorService;
    @Autowired
    private OrderFactory orderFactory;

    /**
     * 拒绝退款申请
     *
     * @param map
     * @return
     */
    @Override
    public ReturnEntity refuseToRefund(Map<String, String> map) {
        String orderId = map.get(Const.ORDER_ID);
        LogUtils.create().methodName("refuseToRefund").key(Const.ORDER).addValue(Const.ORDER_ID, orderId).message("拒绝退款").info();
        OrderResponse orderResponse = getOrder(orderId);
        if (null == orderResponse) {
            return new ReturnEntity(500, Const.ORDER_NOT_EXISTS);
        }
        if(orderResponse.getRefundStatus() != Const.ONE && orderResponse.getRefundStatus() != Const.TWO){
            return new ReturnEntity(500,"操作不合法");
        }
        redisService.zrem(Const.REFUND+orderResponse.getStoreId()+0,orderId);
        redisService.zrem(Const.REFUND+orderResponse.getStoreId()+0,orderId);
        orderResponse.setRefundStatus(Const.NINE);
        /**
         * 如果订单取消了配送
         */
        if (orderResponse.getTotalStatus() == Const.NINETY_SEVEN) {
            orderResponse.setTotalStatus(Const.TWO);
            orderResponse.setSendStatus(Const.TWO);
            /**
             * 重新呼单
             */
            delayJob.createDelayFlowJob(orderId);
        }
        /**
         * 添加拒绝退款记录标识
         */
        if (null != orderResponse.getRefundResponse()) {
            orderResponse.getRefundResponse().setApplyRefundPrice(0);
        }

        orderResponse.setRefundStatus(Const.NINE);
        redisService.zadd(Const.REFUND+orderResponse.getStoreId()+Const.ONE,System.currentTimeMillis()*0.001,orderId,28800);
        notifyService.sendNotify(orderResponse);
        /**
         * 修改order模块订单数据
         */
        ModifyOrderRequest modifyOrderRequest = new ModifyOrderRequest();
        modifyOrderRequest.setOrderId(orderId);
        modifyOrderRequest.setRefundStatus(Const.NINE);
        executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
        return new ReturnEntity();
    }

    /**
     * 订单退款相关业务,包括全额退款以及部分退款
     *
     * @param json
     * @return
     */
    @Override
    public ReturnEntity orderRefund(JSONObject json) {
        String orderId = json.getString(Const.ORDER_ID);
        int price = json.getInteger(Const.PRICE);
        LogUtils.create().methodName("orderRefund").key(Const.ORDER).addValue(Const.ORDER_ID, orderId).message("同意退款").info();
        OrderResponse orderResponse = getOrder(orderId);
        if (null == orderResponse) {
            return new ReturnEntity(500, Const.ORDER_NOT_EXISTS);
        }
        if(price > orderResponse.getPayPrice()){
            return new ReturnEntity(500,"退款金额有误");
        }
        if(null == orderResponse.getRefundResponse()){
            orderResponse.setRefundResponse(new RefundResponse());
        }
        Integer totalStatus = orderResponse.getTotalStatus();
        JSONArray product = null;
        int status = orderResponse.getExceptionStatus() == 99?99:totalStatus;
        if(price > 0){
            if(totalStatus == Const.NINETY_SEVEN){
                orderResponse.setTotalStatus(Const.TWO);
                orderResponse.setSendStatus(Const.TWO);
                delayJob.createDelayFlowJob(orderId);
            }
            product = json.getJSONArray("product");
        }
        /**
         * 设置退款金额
         */
        orderResponse.getRefundResponse().setApplyRefundPrice(price);
        /**
         * 退款操作
         */
        orderFactory.createOrder()
                .setCancelOrder(orderFactory.createCancel(status,price))
                .setRefundOrder(orderFactory.createRefund(orderResponse.getRefundStatus(),price))
                .cancel(orderResponse)
                .refund(orderResponse,product);
        notifyService.sendNotify(orderResponse);
        ModifyOrderRequest modifyOrderRequest = new ModifyOrderRequest();
        modifyOrderRequest.setOrderId(orderId);
        modifyOrderRequest.setOrderStatus(Const.SIX);
        modifyOrderRequest.setSendStatus(orderResponse.getSendStatus());
        modifyOrderRequest.setRefundStatus(orderResponse.getRefundStatus());
        executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
        return new ReturnEntity();
    }

    /**
     * 根据订单id获取订单信息
     * @param orderId
     * @return
     */
    private OrderResponse getOrder(String orderId) {
        /**
         * 先从ehcache中获取
         */
        OrderResponse orderResponse = (OrderResponse) ehCacheService.getObj(orderId);
        if (null == orderResponse) {
            /**
             * 再从redis中获取
             */
            orderResponse = redisService.getObject(orderId, OrderResponse.class);
        }
        if (null == orderResponse) {
            /**
             * 从order数据库获取
             */
            ReturnEntity entity = orderClient.detail(orderId);
            try{
                orderResponse = null == entity || null == entity.getData() ? null : Optional.ofNullable(MapperUtils.json2list(MapperUtils.obj2json(entity.getData()), OrderResponse.class)).get().get(0);
            }catch (Exception e){
                LogUtils.create().methodName("getOrder").key(orderId).type(Const.REFUND).message(e.getMessage()).error();
            }
        }
        return orderResponse;
    }
}
