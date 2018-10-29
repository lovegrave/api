package cn.yanss.m.cookhouse.api.disruptor.handler;


import cn.yanss.m.cookhouse.api.dao.OrderDao;
import cn.yanss.m.cookhouse.api.disruptor.pojo.NotifyMessage;
import cn.yanss.m.util.LogUtils;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

public class NotifyDataBaseEventHandler implements EventHandler<NotifyMessage>, WorkHandler<NotifyMessage> {

    private final OrderDao orderDao;

    public NotifyDataBaseEventHandler(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @Override
    public void onEvent(NotifyMessage notifyMessage, long l, boolean b) throws Exception {
        this.onEvent(notifyMessage);
    }

    @Override
    public void onEvent(NotifyMessage notifyMessage) throws Exception {
        LogUtils.create().methodName("NotifyDataBaseEventHandler").key(notifyMessage.getOrderResponse().getOrderId()).message("订单流程操作").info();
        orderDao.addOrder(notifyMessage.getOrderResponse());
    }
}
