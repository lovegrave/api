package cn.yanss.m.cookhouse.api.order.status;

import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.order.strategy.CancelOrder;
import cn.yanss.m.cookhouse.api.order.strategy.ProcessOrder;
import cn.yanss.m.cookhouse.api.order.strategy.QueryOrder;
import cn.yanss.m.cookhouse.api.order.strategy.RefundOrder;
import com.alibaba.fastjson.JSONArray;

import java.util.List;

/**
 * @desc 订单状态模型超类
 * @date 2018-10-11
 * @author HL
 */
public abstract class OrderModel {

    protected CancelOrder cancelOrder;
    protected ProcessOrder processOrder;
    protected RefundOrder refundOrder;
    protected List<QueryOrder> queryOrders;


    /**
     * 正常流程操作
     * @param orderResponse
     */
    public abstract OrderModel process(OrderResponse orderResponse);

    /**
     * 取消配送操作
     * @param orderResponse
     */
    public abstract OrderModel cancel(OrderResponse orderResponse);

    /**
     * 退款处理
     */
    public abstract OrderModel refund(OrderResponse orderResponse, JSONArray array);

    public CancelOrder getCancelOrder() {
        return cancelOrder;
    }

    public abstract OrderModel setCancelOrder(CancelOrder cancelOrder);


    public abstract OrderModel setProcessOrder(ProcessOrder processOrder);


    public abstract OrderModel setRefundOrder(RefundOrder refundOrder);

    public abstract OrderModel setQueryOrders(List<QueryOrder> queryOrders);

}
