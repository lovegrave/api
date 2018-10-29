package cn.yanss.m.cookhouse.api.disruptor;

import cn.yanss.m.cookhouse.api.cache.EhCacheServiceImpl;
import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.dao.OrderDao;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.disruptor.exception.NotifyEventHandlerException;
import cn.yanss.m.cookhouse.api.disruptor.factory.NotifyEventFactory;
import cn.yanss.m.cookhouse.api.disruptor.handler.NotifyCacheEventHandler;
import cn.yanss.m.cookhouse.api.disruptor.handler.NotifyDataBaseEventHandler;
import cn.yanss.m.cookhouse.api.disruptor.handler.NotifyKitchenEventHandler;
import cn.yanss.m.cookhouse.api.disruptor.pojo.NotifyMessage;
import cn.yanss.m.cookhouse.api.websocket.PushMessage;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;

/**
 * @author hl
 * @desc disruptor 配置启动类，以及事件
 */
@Service
public class NotifyServiceImpl implements DisposableBean, InitializingBean {
    private static final int RING_BUFFER_SIZE = 1024 * 1024;
    private final EhCacheServiceImpl ehCacheService;
    private final RedisService redisService;
    private final PushMessage pushMessage;
    private final OrderDao orderDao;
    private Disruptor<NotifyMessage> disruptor;

    @Autowired
    public NotifyServiceImpl(EhCacheServiceImpl ehCacheService, RedisService redisService, PushMessage pushMessage, OrderDao orderDao) {
        this.ehCacheService = ehCacheService;
        this.redisService = redisService;
        this.pushMessage = pushMessage;
        this.orderDao = orderDao;
    }

    @Override
    public void destroy() throws Exception {
        disruptor.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        disruptor = new Disruptor<>(new NotifyEventFactory(), RING_BUFFER_SIZE, Executors.defaultThreadFactory(), ProducerType.SINGLE, new BlockingWaitStrategy());
        disruptor.setDefaultExceptionHandler(new NotifyEventHandlerException());
        EventHandlerGroup<NotifyMessage> handlerGroup = disruptor.handleEventsWith(new NotifyCacheEventHandler(ehCacheService, redisService));
        handlerGroup.then(new NotifyKitchenEventHandler(pushMessage, redisService), new NotifyDataBaseEventHandler(orderDao));
        disruptor.start();
    }

    public void sendNotify(OrderResponse orderResponse) {
        RingBuffer<NotifyMessage> ringBuffer = disruptor.getRingBuffer();
        /**
         * lambda式写法
         */
        ringBuffer.publishEvent((event, sequence, data) -> event.setOrderResponse(data), orderResponse);

    }
}
