package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.cache.EhCacheServiceImpl;
import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.job.DelayJob;
import cn.yanss.m.cookhouse.api.order.strategy.CancelOrder;
import cn.yanss.m.cookhouse.api.service.DispatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 申请退款时,预订取消配送
 * @author HL
 */
@Component
public class CancelBookDeliverImpl implements CancelOrder {

    @Autowired
    private RedisService redisService;
    @Autowired
    private DelayJob delayJob;
    @Autowired
    private EhCacheServiceImpl ehCacheService;
    @Autowired
    private DispatcherService dispatcherService;

    /**
     * 全额退款申请,预取消配送
     * @param orderResponse
     */
    @Override
    public void cancel(OrderResponse orderResponse) {
        if (null == orderResponse) {
            return;
        }
        /**
         * 如果订单状态为为接单
         */
        if (orderResponse.getTotalStatus() == Const.ONE) {
            /**
             *
             */
            Long len = redisService.zrem(Const.ORDER_STATUS+orderResponse.getStoreId()+1,orderResponse.getOrderId());
            boolean isOk = len == 1 && delayJob.removeJob(orderResponse.getOrderId());
            if (isOk){
                orderResponse.setTotalStatus(97);
                orderResponse.setSendStatus(99);
            }else{
                orderResponse = getOrder(orderResponse.getOrderId());
            }
        }
        /**
         * 当订单状态为以接单时
         */
        if (orderResponse.getTotalStatus() == Const.TWO) {
            /**
             * 如果是自配送
             */
            if (null != orderResponse.getSendType() && 3 == orderResponse.getSendType()) {
                orderResponse.setTotalStatus(97);
                orderResponse.setSendStatus(99);
                return;
            }
            /**
             * 取消呼单任务是否成功
             */
            Long len = redisService.zrem(Const.ORDER_STATUS+orderResponse.getStoreId()+2,orderResponse.getOrderId());
            Boolean isOk =  len == 1 && delayJob.removeJob(orderResponse.getOrderId());
            if (isOk){
                orderResponse.setTotalStatus(97);
                orderResponse.setSendStatus(99);
            }else{
                orderResponse = getOrder(orderResponse.getOrderId());
            }
        }
        /**
         * 如果订单状态为已呼单
         */
        if (orderResponse.getTotalStatus() == Const.THREE) {
            /**
             * 查看订单是否已发往第三方公司
             */
            Boolean isOk = redisService.sismember(Const.FLOW + 1, orderResponse.getOrderId());
            if (isOk) {
                if(dispatcherService.cancelOrder(orderResponse).getCode() == 200){
                    orderResponse.setSendStatus(99);
                    orderResponse.setTotalStatus(97);
                }
                return;
            } else {
                /**
                 * 删除配送是否成功
                 */
                Long len = redisService.srem(Const.FLOW, orderResponse.getOrderId());
                if (len == 1) {
                    redisService.srem(Const.FLOW + 1, orderResponse.getOrderId());
                    orderResponse.setTotalStatus(97);
                    orderResponse.setSendStatus(99);
                    return;
                }
            }
        }
        /**
         * 如果订单状态为 待分配配送员
         */
        if (orderResponse.getTotalStatus() == Const.FOUR) {
            if(dispatcherService.cancelOrder(orderResponse).getCode() == 200){
                orderResponse.setTotalStatus(97);
                orderResponse.setSendStatus(99);
                return;
            }
            orderResponse = getOrder(orderResponse.getOrderId());
        }
        if(orderResponse.getTotalStatus() == Const.NINE){
            Long len = redisService.zrem(Const.ORDER_STATUS+orderResponse.getStoreId()+9,orderResponse.getOrderId());
            if(len == 1){
                orderResponse.setTotalStatus(97);
                orderResponse.setSendStatus(99);
            }
        }
    }

    private OrderResponse getOrder(String orderId) {
        OrderResponse orderResponse = (OrderResponse) ehCacheService.getObj(orderId);
        if (null == orderResponse) {
            orderResponse = redisService.getObject(orderId, OrderResponse.class);
        }
        return orderResponse;
    }

}
