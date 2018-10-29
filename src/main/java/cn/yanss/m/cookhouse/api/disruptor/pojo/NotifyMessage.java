package cn.yanss.m.cookhouse.api.disruptor.pojo;


import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import lombok.Data;

/**
 * 消费实体
 */
@Data
public class NotifyMessage {

    private OrderResponse orderResponse;
}
