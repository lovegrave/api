package cn.yanss.m.cookhouse.api.order.status.impl;

import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.order.status.OrderModel;
import cn.yanss.m.cookhouse.api.order.strategy.CancelOrder;
import cn.yanss.m.cookhouse.api.order.strategy.ProcessOrder;
import cn.yanss.m.cookhouse.api.order.strategy.QueryOrder;
import cn.yanss.m.cookhouse.api.order.strategy.RefundOrder;
import com.alibaba.fastjson.JSONArray;

import java.util.List;

/**
 * 自营平台订单
 */
public class OrderSelfImpl extends OrderModel {

    public OrderSelfImpl(CancelOrder cancelOrder, ProcessOrder processOrder, RefundOrder refundOrder) {
        this.cancelOrder = cancelOrder;
        this.processOrder = processOrder;
        this.refundOrder = refundOrder;
    }

    public OrderSelfImpl() {
    }

    @Override
    public OrderModel process(OrderResponse orderResponse) {
        processOrder.process(orderResponse);
        return this;
    }

    @Override
    public OrderModel cancel(OrderResponse orderResponse) {
        cancelOrder.cancel(orderResponse);
        return this;
    }

    @Override
    public OrderModel refund(OrderResponse orderResponse, JSONArray array) {
        refundOrder.refund(orderResponse,array);
        return this;
    }

    @Override
    public OrderModel setCancelOrder(CancelOrder cancelOrder) {
        this.cancelOrder = cancelOrder;
        return this;
    }

    @Override
    public OrderModel setProcessOrder(ProcessOrder processOrder) {
        this.processOrder = processOrder;
        return this;
    }

    @Override
    public OrderModel setRefundOrder(RefundOrder refundOrder) {
        this.refundOrder = refundOrder;
        return this;
    }

    @Override
    public OrderModel setQueryOrders(List<QueryOrder> queryOrders) {
        this.queryOrders = queryOrders;
        return this;
    }


}
