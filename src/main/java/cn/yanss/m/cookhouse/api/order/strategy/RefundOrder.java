package cn.yanss.m.cookhouse.api.order.strategy;

import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import com.alibaba.fastjson.JSONArray;

public interface RefundOrder {

    void refund(OrderResponse orderResponse, JSONArray array);
}
