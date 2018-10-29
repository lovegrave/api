package cn.yanss.m.cookhouse.api.dao;


import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.entity.HistoryRequest;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OrderDao {

    void addOrder(OrderResponse orderResponse);

    List<Map<String, Object>> queryList(String orderId);

    List<Map<String, Object>> list(Date startTime, Date endTime);

    List<String> getOrderId();

    List<String> history(HistoryRequest historyRequest);
}
