package cn.yanss.m.cookhouse.api.service.impl;

import cn.yanss.m.cookhouse.api.cache.EhCacheServiceImpl;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.dao.entity.StoreResponse;
import cn.yanss.m.cookhouse.api.entity.ModifyOrderRequest;
import cn.yanss.m.cookhouse.api.factory.OrderFactory;
import cn.yanss.m.cookhouse.api.feign.OrderClient;
import cn.yanss.m.cookhouse.api.feign.ProductClient;
import cn.yanss.m.cookhouse.api.service.OrderService;
import cn.yanss.m.cookhouse.api.service.thread.OrderModifyTask;
import cn.yanss.m.cookhouse.api.utils.MapperUtils;
import cn.yanss.m.entity.ReturnEntity;
import cn.yanss.m.util.LogUtils;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderFactory orderFactory;
    @Autowired
    private EhCacheServiceImpl ehCacheService;
    @Autowired
    private ExecutorService executorService;
    @Autowired
    private OrderClient orderClient;
    @Autowired
    private ProductClient productClient;

    @Override
    public ReturnEntity create(JSONArray array) {
        List<OrderResponse> orders = MapperUtils.json2list(MapperUtils.obj2json(array),OrderResponse.class);
        if(orders.isEmpty()){
            return new ReturnEntity();
        }
        for (OrderResponse orderResponse : orders) {
            if (orderResponse.getOrderStatus() > Const.ONE || ehCacheService.exists(orderResponse.getOrderId())) {
                LogUtils.create().methodName("create").key(orderResponse.getOrderId()).message("非推送订单状态,请确认接口").info();
                continue;
            }
            Long serviceTime = orderResponse.getServiceTime().getTime();
            if (serviceTime > System.currentTimeMillis() + 9000000) {
                LogUtils.create().methodName("create").addValue(Const.ORDER_ID,orderResponse.getOrderId()).message("预订单延迟推送").info();
                continue;
            }
            if (serviceTime + 7200000 < System.currentTimeMillis()) {
                /**
                 * 订单超时,做退款业务
                 */
                LogUtils.create().methodName("create").message("订单超时").addValue(Const.ORDER_ID,orderResponse.getOrderId()).info();
                ModifyOrderRequest modifyOrderRequest = new ModifyOrderRequest();
                modifyOrderRequest.setOrderId(orderResponse.getOrderId());
                modifyOrderRequest.setExceptionRemark("订单超时");
                modifyOrderRequest.setDeliveryId("");
                modifyOrderRequest.setOrderStatus(Const.SIX);
                orderFactory.createOrder().setRefundOrder(orderFactory.createRefund(Const.ZERO,Const.ZERO))
                        .refund(orderResponse,null);
                modifyOrderRequest.setRefundStatus(orderResponse.getRefundStatus());
                executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
                continue;
            }
            if (orderResponse.getBookingType() == Const.ONE) {
                /**
                 * 预定单处理,在营业时间，可以推送，在非营业时间，不能推送
                 */
                ReturnEntity entity = productClient.findStore(orderResponse.getStoreId());
                if (null == entity || null == entity.getData()) {
                    LogUtils.create().methodName("create").message("订单对应店铺不存在或者查询店铺失败").error();
                    continue;
                }
                StoreResponse storeResponse = MapperUtils.obj2pojo(entity.getData(), StoreResponse.class);
                List<StoreResponse.Busniss> businessTime = storeResponse.getBusinessTime();
                businessTime.get(0);

            } else {
                LogUtils.create().methodName("addOrder").message("订单创建").addValue(Const.ORDER_ID,orderResponse.getOrderId()).info();
                orderFactory.createOrder().setProcessOrder(orderFactory.createProcess(Const.ZERO))
                        .process(orderResponse);
            }
        }
        return new ReturnEntity();
    }
}
