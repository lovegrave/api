package cn.yanss.m.cookhouse.api.service.impl;

import cn.yanss.m.cookhouse.api.cache.EhCacheServiceImpl;
import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.disruptor.NotifyServiceImpl;
import cn.yanss.m.cookhouse.api.entity.ModifyOrderRequest;
import cn.yanss.m.cookhouse.api.feign.DispatcherClient;
import cn.yanss.m.cookhouse.api.feign.OrderClient;
import cn.yanss.m.cookhouse.api.job.DelayJob;
import cn.yanss.m.cookhouse.api.service.DispatcherService;
import cn.yanss.m.cookhouse.api.service.thread.OrderModifyTask;
import cn.yanss.m.cookhouse.api.utils.MapperUtils;
import cn.yanss.m.entity.ReturnEntity;
import cn.yanss.m.util.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

@Service
public class DispatcherServiceImpl implements DispatcherService {

    private final OrderClient orderClient;
    private final DispatcherClient dispatcherClient;
    private final RedisService redisService;
    private final EhCacheServiceImpl ehCacheService;
    private final NotifyServiceImpl notifyService;
    private final ExecutorService executorService;
    @Autowired
    private DelayJob delayJob;
    @Value("${delivery.times}")
    private Integer times;

    @Autowired
    public DispatcherServiceImpl(OrderClient orderClient, DispatcherClient dispatcherClient, RedisService redisService, EhCacheServiceImpl ehCacheService, NotifyServiceImpl notifyService, ExecutorService executorService) {
        this.orderClient = orderClient;
        this.dispatcherClient = dispatcherClient;
        this.redisService = redisService;
        this.ehCacheService = ehCacheService;
        this.notifyService = notifyService;
        this.executorService = executorService;
    }

    @Override
    public ReturnEntity gainOrder() {
        Set<String> orderIds = redisService.sdiff(Const.FLOW, Const.FLOW + 1);
        List<String> list = new ArrayList<>();
        orderIds.stream().forEach(d->{
            LogUtils.create().addValue(Const.ORDER_ID,d).methodName("gainOrder").message("发往第三方公司").info();
            redisService.smove(Const.FLOW, Const.FLOW + 1, d, 36000);
            list.add(d);
        });
        return new ReturnEntity(ehCacheService.getList(list));
    }

    @Override
    public void flow(String orderId) {
        redisService.smove(Const.FLOW+1, Const.FLOW, orderId, 3600);
    }

    @Override
    public ReturnEntity status(String orderId) {
        OrderResponse orderResponse = queryOrder(orderId);
        if(null == orderResponse){
            return new ReturnEntity();
        }
        return dispatcherClient.queryOrder(orderResponse);
    }

    /**
     * 新版取消配送
     * @param orderResponse
     * @return
     */
    @Override
    public ReturnEntity cancelOrder(OrderResponse orderResponse) {
        LogUtils.create().methodName("cancelOrder").message("取消配送").addValue(Const.ORDER_ID,orderResponse.getOrderId()).info();
        return dispatcherClient.cancelCode(orderResponse);
    }

    @Override
    public void orderCancel(ModifyOrderRequest modifyOrderRequest) {
        LogUtils.create().methodName("DispatcherServiceImpl-->orderCancel").key(modifyOrderRequest.getOrderId()).message("取消订单").info();
        modifyOrderRequest.setOrderStatus(6);
        modifyOrderRequest.setSendStatus(99);
        redisService.srem(Const.FLOW, modifyOrderRequest.getOrderId());
        redisService.srem(Const.FLOW + 1, modifyOrderRequest.getOrderId());
        delayJob.removeJob(modifyOrderRequest.getOrderId());
        executorService.submit(new OrderModifyTask(modifyOrderRequest,orderClient));
    }

    /**
     * 订单完结
     *
     * @param orderId
     */
    @Override
    public void over(String orderId) {
        LogUtils.create().methodName("DispatcherServiceImpl-->over").key(orderId).message("订单超时自动完成").info();
        OrderResponse orderResponse = (OrderResponse) ehCacheService.getObj(orderId);
        ModifyOrderRequest modifyOrderRequest = new ModifyOrderRequest();
        modifyOrderRequest.setOrderId(orderId);
        modifyOrderRequest.setOrderStatus(Const.FIVE);
        modifyOrderRequest.setCommentStatus(1);
        executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
        if (null == orderResponse) {
            LogUtils.create().methodName("DispatcherServiceImpl-->over").key(orderId).message(Const.ORDER_NOT_EXISTS).info();
            return;
        }
        orderResponse.setTotalStatus(Const.EIGHT);
        orderResponse.setOrderStatus(Const.FIVE);
        notifyService.sendNotify(orderResponse);
    }

    /**
     * 订单运行时异常,该异常是厨房端需要注意的异常，不一定是必须要处理的异常
     * 修改的属性：异常编码,异常信息,报异常时间
     *
     * @return
     */
    @Override
    public void error(ModifyOrderRequest modifyOrderRequest) {
        String orderId = modifyOrderRequest.getOrderId();
        LogUtils.create().methodName("DispatcherServiceImpl-->error").key(orderId).message("订单出现配送中异常").info();
        OrderResponse orderResponse = queryOrder(orderId);
        if (null == orderResponse) {
            LogUtils.create().methodName("DispatcherServiceImpl-->error").key(orderId).message(Const.ORDER_NOT_EXISTS).info();
            return;
        }
        /**
         * 删除该订单发配送的调度缓存
         */
        redisService.srem(Const.FLOW, orderId);
        /**
         * 将要修改的的值赋值给orderResponse
         */
        orderResponse.setExceptionStatus(Const.ONE);
        orderResponse.setExceptionRemark(orderResponse.getExceptionRemark() + modifyOrderRequest.getExceptionRemark());
        orderResponse.setSendExceptionTime(new Date());
        /**
         * 将变化后的订单状态存入redis sorted set(允许查询一次)
         */
        notifyService.sendNotify(orderResponse);
    }

    /**
     * 该异常是由于配送不通过，或者第三方公司取消订单等原因,急需处理的订单
     * 修改属性：异常编码,异常信息,以及时间
     *
     * @param modifyOrderRequest
     * @return
     */
    @Override
    public void anomaly(ModifyOrderRequest modifyOrderRequest) {
        LogUtils.create().methodName("DispatcherServiceImpl-->anomaly").key(modifyOrderRequest.getOrderId()).message("订单出现配送失败异常").info();
        String orderId = modifyOrderRequest.getOrderId();
        OrderResponse orderResponse = queryOrder(orderId);
        if (null == orderResponse) {
            LogUtils.create().methodName("DispatcherServiceImpl-->anomaly").key(orderId).message(Const.ORDER_NOT_EXISTS).info();
            return;
        }
        Integer count = orderResponse.getTimes() + 1;
        /**
         * 记录是第几次配送
         */
        orderResponse.setTimes(count);
        /**
         * 删除配送
         */
        redisService.srem(Const.FLOW, orderId);
        /**
         * 删除发配送记录
         */
        redisService.srem(Const.FLOW + 1, orderId);
        /**
         * 如果连续配送times+1次失败,请厨房端处理
         */
        if (count > times) {
            /**
             * 将该订单从发起配送状态中删除
             */
            orderResponse.setTotalStatus(Const.NINE);
            orderResponse.setSendStatus(99);
            /**
             * 如果达到重复次数,让用户感知订单异常
             */
            orderResponse.setExceptionStatus(Const.TWO);
            /**
             * 同步数据库
             */
            executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
        } else {
            /**
             * 重新发起配送,不让厨房端感知订单异常
             */

            redisService.sadd(Const.FLOW, 7200, orderId);
            orderResponse.setTotalStatus(Const.THREE);
        }
        orderResponse.setExceptionRemark(modifyOrderRequest.getExceptionRemark());
        /**
         * 将新的订单状态存入缓存,并发给厨房
         */
        notifyService.sendNotify(orderResponse);
    }

    /**
     * 订单已完成的接口,当骑手完成订单,可以使用改接口回调
     * 修改属性：送达时间,送餐员姓名,骑手电话,订单状态,配送状态
     *
     * @param modifyOrderRequest
     * @return
     */
    @Override
    public void complete(ModifyOrderRequest modifyOrderRequest) {
        LogUtils.create().methodName("DispatcherServiceImpl-->complete").key(modifyOrderRequest.getOrderId()).message("订单配送已完成").info();
        OrderResponse orderResponse = queryOrder(modifyOrderRequest.getOrderId());
        if (null == orderResponse) {
            LogUtils.create().methodName("DispatcherServiceImpl-->complete").key(modifyOrderRequest.getOrderId()).message(Const.ORDER_NOT_EXISTS).info();
            return;
        }
        orderResponse.setTotalStatus(Const.SEVEN);
        orderResponse.setOrderStatus(Const.FOUR);
        orderResponse.setSendStatus(Const.SIX);
        orderResponse.setTaskTime(new Date());
        orderResponse.setRiderName(modifyOrderRequest.getRiderName());
        orderResponse.setRiderPhone(modifyOrderRequest.getRiderPhone());
        notifyService.sendNotify(orderResponse);
        executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
        delayJob.createDelayOverJob(orderResponse.getOrderId());
        redisService.zrem(Const.REMINDER + orderResponse.getStoreId(), orderResponse.getOrderId());
    }

    /**
     * 骑手到店或者取货接口,该回调是骑手已经到店，已经接受订单
     *
     * @param modifyOrderRequest
     * @return
     */
    @Override
    public void pickup(ModifyOrderRequest modifyOrderRequest) {
        LogUtils.create().methodName("DispatcherServiceImpl-->pickup").key(modifyOrderRequest.getOrderId()).message("骑手已到店,订单配送中").info();
        OrderResponse orderResponse = queryOrder(modifyOrderRequest.getOrderId());
        if (null == orderResponse) {
            LogUtils.create().methodName("DispatcherServiceImpl-->pickup").key(modifyOrderRequest.getOrderId()).message(Const.ORDER_NOT_EXISTS).info();
            return;
        }
        orderResponse.setTotalStatus(Const.SIX);
        orderResponse.setOrderStatus(Const.THREE);
        orderResponse.setSendStatus(Const.FIVE);
        orderResponse.setRiderName(modifyOrderRequest.getRiderName());
        orderResponse.setRiderPhone(modifyOrderRequest.getRiderPhone());
        orderResponse.setPickupTime(new Date());
        notifyService.sendNotify(orderResponse);
        executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
    }

    /**
     * 骑手已接单,该回调是第三方公司发完配送，骑手已经接单的回调
     *
     * @param modifyOrderRequest
     * @return
     */
    @Override
    public void taskOrder(ModifyOrderRequest modifyOrderRequest) {
        LogUtils.create().methodName("DispatcherServiceImpl-->taskOrder").key(modifyOrderRequest.getOrderId()).message("骑手已接单").info();
        OrderResponse orderResponse = queryOrder(modifyOrderRequest.getOrderId());
        if (null == orderResponse) {
            LogUtils.create().methodName("DispatcherServiceImpl-->taskOrder").key(modifyOrderRequest.getOrderId()).message(Const.ORDER_NOT_EXISTS).info();
            return;
        }
        orderResponse.setSendStatus(Const.FOUR);
        orderResponse.setTotalStatus(Const.FIVE);
        orderResponse.setRiderName(modifyOrderRequest.getRiderName());
        orderResponse.setRiderPhone(modifyOrderRequest.getRiderPhone());
        orderResponse.setRiderPackTime(new Date());
        notifyService.sendNotify(orderResponse);
        executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
    }

    /**
     * 订单发起配送成功,该回调由第三方公司接受订单后发起的回调
     *
     * @param modifyOrderRequest
     * @return
     */
    @Override
    public void haveOrder(ModifyOrderRequest modifyOrderRequest) {
        LogUtils.create().methodName("DispatcherServiceImpl-->haveOrder").key(modifyOrderRequest.getOrderId()).message("订单发起配送成功").info();
        OrderResponse orderResponse = queryOrder(modifyOrderRequest.getOrderId());
        if (null == orderResponse) {
            LogUtils.create().methodName("DispatcherServiceImpl-->haveOrder").key(modifyOrderRequest.getOrderId()).message(Const.ORDER_NOT_EXISTS).info();
            return;
        }
        orderResponse.setSendType(modifyOrderRequest.getSendType());
        orderResponse.setDeliveryId(modifyOrderRequest.getDeliveryId());
        orderResponse.setSendStatus(Const.THREE);
        orderResponse.setTotalStatus(Const.FOUR);
        orderResponse.setSendId(null != modifyOrderRequest.getSendId() ? modifyOrderRequest.getSendId() : null);
        orderResponse.setMtPeisongId(null != modifyOrderRequest.getMtPeisongId() ? modifyOrderRequest.getMtPeisongId() : null);
        redisService.srem(Const.FLOW, modifyOrderRequest.getOrderId());
        notifyService.sendNotify(orderResponse);
        executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
    }

    @Override
    public ReturnEntity findRiderLocation(String orderId) {
        OrderResponse orderResponse = queryOrder(orderId);
        return dispatcherClient.findRiderLocation(orderResponse);
    }

    @Override
    public void update(ModifyOrderRequest modifyOrderRequest) {
        LogUtils.create().methodName("DispatcherServiceImpl-->update").key(modifyOrderRequest.getOrderId()).message("订单回调修改信息").info();
        OrderResponse orderResponse = queryOrder(modifyOrderRequest.getOrderId());
        if (null == orderResponse) {
            LogUtils.create().methodName("DispatcherServiceImpl-->update").key(modifyOrderRequest.getOrderId()).message(Const.ORDER_NOT_EXISTS).info();
            return;
        }
        orderResponse.setExceptionRemark(orderResponse.getExceptionRemark() + modifyOrderRequest.getExceptionRemark());
        orderResponse.setRiderName(modifyOrderRequest.getRiderName());
        orderResponse.setRiderPhone(modifyOrderRequest.getRiderPhone());
        redisService.srem(Const.FLOW, modifyOrderRequest.getOrderId());
        notifyService.sendNotify(orderResponse);
        executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
    }



    /**
     * 提取公共取值方法
     *
     * @param orderId
     * @return
     * @throws IOException
     */
    private OrderResponse queryOrder(String orderId) {
        /**
         * 从ehcache中取值,由于存值的时候是直接存orderResponse对象,取出时可直接强转
         */
        OrderResponse orderResponse = (OrderResponse) ehCacheService.getObj(orderId);
        if (null == orderResponse) {
            orderResponse = redisService.getObject(orderId, OrderResponse.class);
        }
        if (null == orderResponse) {
            /**
             * 从order模块取值
             */
            ReturnEntity entity = orderClient.detail(orderId);
            try {
                orderResponse = null != entity.getData() ? MapperUtils.json2list(MapperUtils.obj2json(entity.getData()), OrderResponse.class).get(0) : null;
            } catch (Exception e) {
                LogUtils.create().key(orderId).methodName("dispatcher->queryOrder").error();
            }
        }
        return orderResponse;
    }

}
