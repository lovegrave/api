package cn.yanss.m.cookhouse.api.factory;

import cn.yanss.m.cookhouse.api.order.status.OrderModel;
import cn.yanss.m.cookhouse.api.order.status.impl.OrderSelfImpl;
import cn.yanss.m.cookhouse.api.order.strategy.CancelOrder;
import cn.yanss.m.cookhouse.api.order.strategy.ProcessOrder;
import cn.yanss.m.cookhouse.api.order.strategy.QueryOrder;
import cn.yanss.m.cookhouse.api.order.strategy.RefundOrder;
import cn.yanss.m.cookhouse.api.order.strategy.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * order 工厂
 */
@Component
public class OrderFactory {
    @Autowired
    private CancelBookDeliverImpl cancelBook;
    @Autowired
    private CancelNoImpl cancelNo;
    @Autowired
    private CancelCallImpl cancelCall;
    @Autowired
    private CancelDeliveryImpl cancelDelivery;
    @Autowired
    private ProcessCallImpl processCall;
    @Autowired
    private ProcessNoImpl processNo;
    @Autowired
    private ProcessPaidImpl processPaid;
    @Autowired
    private ProcessReceiveImpl processReceive;
    @Autowired
    private ProcessReconnectImpl processReconnect;
    @Autowired
    private RefundFullImpl refundFull;
    @Autowired
    private RefundNoImpl refundNo;
    @Autowired
    private RefundPartImpl refundPart;
    @Autowired
    private QueryRefundImpl queryRefund;
    @Autowired
    private QueryHistoryImpl queryHistory;
    @Autowired
    private QueryNoImpl queryNo;
    @Autowired
    private QueryProcessImpl queryProcess;
    @Autowired
    private QueryReminderImpl queryReminder;

    /**
     * 取消配送
     * @param status
     * @return
     */
    public CancelOrder createCancel(int status,int price){
        if(price > 0){
            return cancelNo;
        }
        if(status == 2){
            return cancelCall;
        }
        if(status == 96){
            return cancelBook;
        }
        if(status == 97 || status == 9){
            return cancelNo;
        }else{
            return cancelDelivery;
        }
    }

    /**
     * 流程操作
     * @param type
     * @return
     */
    public ProcessOrder createProcess(int type){
        switch (type){
            case 0:
                return processPaid;
            case 1:
                return processReceive;
            case 2:
                return processCall;
            case 9:
                return processReconnect;
            default:
                return processNo;
        }
    }

    /**
     *
     * @param type 1.同意退款 其他：不同意或者待确认
     * @param price 0.全额退款 其它：部分退款
     * @return
     */
    public RefundOrder createRefund(int type,int price){
        switch (type){
            /**
             * 申请退款时,如果订单未接单,返回refundFull,否则需工作人员同意,返回refundNo
             */
            case 0: return price == 0 ? refundFull:refundNo;
            /**
             * 在订单退款状态为1(申请全额退款),2(申请部分退款),9(拒绝退款)
             */
            case 1: return price == 0 ? refundFull:refundPart;
            case 2: return price == 0 ? refundFull:refundPart;
            case 9: return price == 0 ? refundFull:refundPart;
            /**
             * 订单退款状态为19(全额退款失败),再次同意退款则必须是全额退款
             */
            case 19:return refundFull;
            /**
             * 订单状态为29(部分退款失败),可以全额退款,也可以部分退款
             */
            case 29: return price == 0 ? refundFull:refundPart;
            default: return refundNo;
        }
    }

    /**
     * 实时查询
     * @param reminderStatus
     * @param refundStatus
     * @return
     */
    public QueryOrder createQuery(Integer reminderStatus,Integer refundStatus){
        if(null != refundStatus){
            return queryRefund;
        }
        if(null != reminderStatus && reminderStatus != 0){
            return queryReminder;
        }
        return queryProcess;
    }
    public QueryOrder getHis(){
        return queryHistory;
    }
    /**
     * 创建订单
     * @return
     */
    public OrderModel createOrder(){
        return new OrderSelfImpl();
    }


}
