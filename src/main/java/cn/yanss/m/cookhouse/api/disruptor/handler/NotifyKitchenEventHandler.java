package cn.yanss.m.cookhouse.api.disruptor.handler;


import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.disruptor.pojo.NotifyMessage;
import cn.yanss.m.cookhouse.api.websocket.PushMessage;
import cn.yanss.m.util.LogUtils;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import org.springframework.util.StringUtils;

import java.util.Collections;

/**
 * @author hl
 * @desc 消费者
 */
public class NotifyKitchenEventHandler implements EventHandler<NotifyMessage>, WorkHandler<NotifyMessage> {

    private final PushMessage pushMessage;
    private final RedisService redisService;

    public NotifyKitchenEventHandler(PushMessage pushMessage, RedisService redisService) {
        this.pushMessage = pushMessage;
        this.redisService = redisService;
    }

    @Override
    public void onEvent(NotifyMessage notifyMessage, long l, boolean b) throws Exception {
        this.onEvent(notifyMessage);
    }

    @Override
    public void onEvent(NotifyMessage notifyMessage) throws Exception {
        /**
         * 厨房端逻辑处理
         */
        OrderResponse orderResponse = notifyMessage.getOrderResponse();
        LogUtils.create().methodName("NotifyKitchenEventHandler").key(orderResponse.getOrderId()).message("数据推送").info();
        String channelId = redisService.getString(Const.ROLE_USER + orderResponse.getStoreId());
        if (StringUtils.isEmpty(channelId)) {
            redisService.lrem(Const.NETTY_PUSH, orderResponse.getOrderId());
            redisService.lpush(Const.NETTY_PUSH, orderResponse.getOrderId(), 3600);
            LogUtils.create().methodName("NotifyKitchenEventHandler").key(orderResponse.getOrderId()).message("token不存在或已过期").error();
        } else {
            pushMessage.sendMessage(channelId, Collections.singletonList(orderResponse), orderResponse.getOrderId());
        }
    }
}
