package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.job.DelayJob;
import cn.yanss.m.cookhouse.api.order.strategy.ProcessOrder;
import cn.yanss.m.cookhouse.api.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 订单已付款(推单)的流程操作
 * @author HL
 */
@Component
public class ProcessPaidImpl implements ProcessOrder {
    @Autowired
    private RedisService redisService;
    @Autowired
    private DelayJob delayJob;
    @Override
    public void process(OrderResponse orderResponse) {
        if (null != orderResponse.getDeliveryType() && orderResponse.getDeliveryType() == Const.TWO) {
            orderResponse.setOrderPick(DateUtil.getRandom());
            redisService.setMapString(Const.ORDER_PICK + orderResponse.getStoreId(), orderResponse.getOrderPick(), orderResponse.getOrderId(), 10800);
        } else {
            redisService.lpush(Const.PHONE + orderResponse.getConsigneeInfo().getString("consigneePhone"), orderResponse.getOrderId(), 10800);
        }
        /**
         * 设置订单流程管理状态
         */
        orderResponse.setTotalStatus(Const.ONE);
        /**
         * 设置
         */
        orderResponse.setBeforeStatus(Const.ONE);
        /**
         * 设置取货号
         */
        String orderNo = orderResponse.getOrderNo();
        if (StringUtils.isEmpty(orderNo)) {
            orderNo = String.valueOf(1000 + redisService.incr(DateUtil.getStartTime() + orderResponse.getStoreId(), 90000));
            orderResponse.setOrderNo(orderNo);
        }
        /**
         * 将取货号与订单号关联,便于订单的查询
         */
        redisService.setMapString(Const.ORDER_PICK + orderResponse.getStoreId(), orderNo, orderResponse.getOrderId(), 10800);

        /**
         * 设置订单配送状态
         */
        orderResponse.setSendStatus(Const.ONE);
        delayJob.createRefundJob(orderResponse.getOrderId());
    }
}
