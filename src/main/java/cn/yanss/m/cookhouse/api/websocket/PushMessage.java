package cn.yanss.m.cookhouse.api.websocket;

import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.utils.MapperUtils;
import cn.yanss.m.cookhouse.api.websocket.link.Global;
import cn.yanss.m.util.LogUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class PushMessage {

    private final RedisService redisService;


    @Autowired
    public PushMessage(RedisService redisService) {
        this.redisService = redisService;
    }

    /**
     * netty 推送
     *
     * @param channelId
     * @param obj
     * @return
     */
    public void sendMessage(String channelId, Object obj, String orderId) {
        Channel channel = Global.get(channelId);
        if (null == channel) {
            LogUtils.create().addValue("storeId",redisService.getString(channelId)).addValue("orderId",orderId).message("没有webSocket连接信息").error();
            redisService.lrem(Const.NETTY_PUSH, orderId);
            redisService.lpush(Const.NETTY_PUSH, orderId, 3600);
            return;
        }
        try {
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(MapperUtils.obj2json(obj));
            ChannelFuture channelFuture = channel.writeAndFlush(textWebSocketFrame);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) {
                    if (channelFuture.isSuccess()) {
                        LogUtils.create().methodName("sendMessage-->operationComplete").key(orderId).message("推送成功").info();
                        redisService.lrem(Const.NETTY_PUSH, orderId);
                    } else {
                        LogUtils.create().methodName("sendMessage-->operationComplete").key(orderId).message("推送失败").error();
                        redisService.lrem(Const.NETTY_PUSH, orderId);
                        redisService.lpush(Const.NETTY_PUSH, orderId, 3600);
                    }
                }
            });
        } catch (Exception e) {
            LogUtils.create().methodName("PushMessage-->sendMessage").message(e.getMessage()).error();
            redisService.lrem(Const.NETTY_PUSH, orderId);
            redisService.lpush(Const.NETTY_PUSH, orderId, 3600);
        }
    }
}
