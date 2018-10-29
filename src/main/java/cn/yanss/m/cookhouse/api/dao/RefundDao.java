package cn.yanss.m.cookhouse.api.dao;


import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;

public interface RefundDao {

    void insertApply(OrderResponse orderResponse, Integer refundPrice, String productId);
}
