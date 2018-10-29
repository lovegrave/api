package cn.yanss.m.cookhouse.api.disruptor.factory;


import cn.yanss.m.cookhouse.api.disruptor.pojo.NotifyMessage;
import com.lmax.disruptor.EventFactory;

/**
 * @author hl
 * @desc disruptor 工厂
 */
public class NotifyEventFactory implements EventFactory<NotifyMessage> {
    @Override
    public NotifyMessage newInstance() {
        return new NotifyMessage();
    }
}
