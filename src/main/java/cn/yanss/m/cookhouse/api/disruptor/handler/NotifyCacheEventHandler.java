package cn.yanss.m.cookhouse.api.disruptor.handler;


import cn.yanss.m.cookhouse.api.cache.EhCacheServiceImpl;
import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.disruptor.pojo.NotifyMessage;
import cn.yanss.m.util.LogUtils;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

/**
 * @author hl
 * @desc disruptor 消费者
 */
public class NotifyCacheEventHandler implements EventHandler<NotifyMessage>, WorkHandler<NotifyMessage> {

    private final EhCacheServiceImpl ehCacheService;
    private final RedisService redisService;

    public NotifyCacheEventHandler(EhCacheServiceImpl ehCacheService, RedisService redisService) {
        this.ehCacheService = ehCacheService;
        this.redisService = redisService;
    }

    @Override
    public void onEvent(NotifyMessage notifyMessage, long l, boolean b) throws Exception {
        this.onEvent(notifyMessage);
    }

    @Override
    public void onEvent(NotifyMessage notifyMessage) throws Exception {
        /**
         * 订单模块逻辑
         */
        OrderResponse orderResponse = notifyMessage.getOrderResponse();
        /**
         * 将订单存储在ehcache缓存中
         */
        LogUtils.create().methodName("NotifyCacheEventHandler").key(orderResponse.getOrderId()).info();
        redisService.zrem(Const.ORDER_STATUS + orderResponse.getStoreId() + orderResponse.getBeforeStatus(), orderResponse.getOrderId());
        /**
         * 将订单状态关联订单号放入redis sorted set 集合中,厨房通过该集合查询该状态的实时订单
         */
        orderResponse.setBeforeStatus(orderResponse.getTotalStatus());
        /**
         * 订单数据存储再本地缓存
         */
        ehCacheService.save(orderResponse.getOrderId(), orderResponse);
        /**
         * 订单数据存储在redis,存储10小时
         */
        redisService.setObject(orderResponse.getOrderId(), orderResponse, 36000);
        redisService.zadd(Const.ORDER_STATUS + orderResponse.getStoreId() + orderResponse.getTotalStatus(), Double.valueOf(orderResponse.getOrderNo()), orderResponse.getOrderId(), 7200);
    }
}
