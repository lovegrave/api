package cn.yanss.m.cookhouse.api.job;

import cn.yanss.m.cookhouse.api.cache.EhCacheServiceImpl;
import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.disruptor.NotifyServiceImpl;
import cn.yanss.m.cookhouse.api.service.DispatcherService;
import cn.yanss.m.util.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Tuple;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * @author hl
 * @date 2018-8-29
 */
@Component
public class DelayJob {

    @Autowired
    private RedisService redisService;
    @Autowired
    private EhCacheServiceImpl ehCacheService;
    @Autowired
    private DispatcherService dispatcherService;
    @Autowired
    private NotifyServiceImpl notifyService;


    @Value("${timer.job.refund}")
    private Integer refundJobTimer;
    @Value("${timer.job.flow}")
    private Integer flowJobTimer;
    @Value("${timer.job.over}")
    private Integer overJobTimer;
    @Value("${timer.job.monitor}")
    private Integer monitorJobTimer;


    /**
     * 订单延迟任务
     */
    @PostConstruct
    public void init() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    Set<Tuple> result = redisService.zrangeScore(Const.TIMER_JOB);
                    if (result.isEmpty()) {
                        continue;
                    }
                    String value = ((Tuple) result.toArray()[0]).getElement();
                    double score = ((Tuple) result.toArray()[0]).getScore();
                    if (score > System.currentTimeMillis()) {
                        continue;
                    }
                    /**
                     * 删除定时任务,如果延迟任务删除成功则继续业务
                     */
                    Long len = redisService.zrem(Const.TIMER_JOB, value);
                    if (len != 1) {
                        continue;
                    }
                    OrderResponse orderResponse = (OrderResponse) ehCacheService.getObj(value);
                    if (null == orderResponse) {
                        orderResponse = redisService.getObject(value, OrderResponse.class);
                    }
                    if (null == orderResponse) {
                        LogUtils.create().methodName("DelayJob-->init").key("order").addValue("orderId", value).message("该订单不存在").info();
                        continue;
                    }
                    Integer totalStatus = orderResponse.getTotalStatus();
                    if (totalStatus == Const.SEVEN) {

                    } else if (totalStatus == 2 && flowJobTimer != 0) {

                    } else if (totalStatus == 1 && refundJobTimer != 0) {


                    } else {
                        LogUtils.create().methodName("DelayJob").key("order").addValue("orderId", value).message("订单超时,请注意订单状态").error();
                    }
                }
            }
        }.start();
    }


    /**
     * 创建订单配送延迟任务
     *
     * @param orderId
     */
    public void createDelayFlowJob(String orderId) {
        LogUtils.create().methodName("createDelayFlowJob").key("order").addValue("orderId", orderId).message("创建订单配送延迟任务").info();
        /**
         * 如果配送延迟时间为0,则为立即配送
         */
        if (flowJobTimer == 0) {
            redisService.sadd(Const.FLOW, 7200, orderId);
            OrderResponse orderResponse = (OrderResponse) ehCacheService.getObj(orderId);
            if (null == orderResponse) {
                orderResponse = redisService.getObject(orderId, OrderResponse.class);
            }
            if (null == orderResponse) {
                LogUtils.create().methodName("createDelayFlowJob").key("order").addValue("orderId", orderId).message("订单记录不存在").info();
                return;
            }
            orderResponse.setTotalStatus(3);
            notifyService.sendNotify(orderResponse);
            return;
        }
        Double score = (System.currentTimeMillis() + flowJobTimer) * 1.0;
        redisService.zadd(Const.TIMER_JOB, score, orderId, 360);
    }

    /**
     * 创建订单用户完成延迟任务
     *
     * @param orderId
     */
    public void createDelayOverJob(String orderId) {
        LogUtils.create().methodName("createDelayOverJob").key("order").addValue("orderId", orderId).message("创建订单用户完成延迟任务").info();
        if (overJobTimer == 0) {
//            dispatcherService.over(orderId);
            return;
        }
        Double score = (System.currentTimeMillis() + overJobTimer) * 1.0;
        redisService.zadd(Const.TIMER_JOB, score, orderId, 14400);
    }

    public void createRefundJob(String orderId) {
        LogUtils.create().methodName("createRefundJob").key("order").addValue("orderId", orderId).message("创建订单超时退款任务").info();
        if (refundJobTimer == 0) {
            return;
        }
        Double score = (System.currentTimeMillis() + refundJobTimer) * 1.0;
        redisService.zadd(Const.TIMER_JOB, score, orderId, 600);

    }

    /**
     * 超时任务模块封装
     * @param orderId
     */
    public void createMonitorJob(String orderId) {
        LogUtils.create().methodName("createMonitorJob").key("order").addValue("orderId", orderId).message("创建订单超时监控任务").info();
        if (monitorJobTimer == 0) {
            return;
        }
        Double score = (System.currentTimeMillis() + monitorJobTimer) * 1.0;
        redisService.zadd(Const.TIMER_JOB, score, orderId, 14400);

    }

    /**
     * 删除延时任务
     *
     * @return
     */
    public boolean removeJob(String orderId) {
        LogUtils.create().methodName("removeJob").key("order").addValue("orderId", orderId).message("删除延迟任务").info();
        Long len = redisService.zrem(Const.TIMER_JOB, orderId);
        return len == 1;
    }

    public boolean exists(String orderId) {
        return 1 == redisService.zrank(Const.TIMER_JOB, orderId);
    }
}
