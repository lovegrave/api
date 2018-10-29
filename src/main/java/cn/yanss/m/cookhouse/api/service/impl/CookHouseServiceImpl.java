package cn.yanss.m.cookhouse.api.service.impl;

import cn.yanss.m.cookhouse.api.anntate.ServiceLimit;
import cn.yanss.m.cookhouse.api.cache.EhCacheServiceImpl;
import cn.yanss.m.cookhouse.api.cache.RedisService;
import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.constant.Limit;
import cn.yanss.m.cookhouse.api.dao.OrderDao;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.dao.entity.StoreResponse;
import cn.yanss.m.cookhouse.api.disruptor.NotifyServiceImpl;
import cn.yanss.m.cookhouse.api.entity.HistoryRequest;
import cn.yanss.m.cookhouse.api.entity.ModifyOrderRequest;
import cn.yanss.m.cookhouse.api.entity.OrderRequest;
import cn.yanss.m.cookhouse.api.factory.OrderFactory;
import cn.yanss.m.cookhouse.api.feign.OrderClient;
import cn.yanss.m.cookhouse.api.feign.ProductClient;
import cn.yanss.m.cookhouse.api.job.DelayJob;
import cn.yanss.m.cookhouse.api.service.CookHouseService;
import cn.yanss.m.cookhouse.api.service.ProductService;
import cn.yanss.m.cookhouse.api.service.thread.OrderModifyTask;
import cn.yanss.m.cookhouse.api.utils.DateUtil;
import cn.yanss.m.cookhouse.api.utils.MapperUtils;
import cn.yanss.m.entity.ReturnEntity;
import cn.yanss.m.exception.MallException;
import cn.yanss.m.util.LogUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class CookHouseServiceImpl implements CookHouseService {

    private final OrderClient orderClient;
    private final ProductClient productClient;
    @Autowired
    private RedisService redisService;
    @Autowired
    private EhCacheServiceImpl ehCacheService;
    @Autowired
    private NotifyServiceImpl notifyService;
    @Autowired
    private ExecutorService executorService;
    @Autowired
    private DelayJob delayJob;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderFactory orderFactory;

    @Autowired
    public CookHouseServiceImpl(OrderClient orderClient, ProductClient productClient) {
        this.orderClient = orderClient;
        this.productClient = productClient;
    }

    /**
     * 手动查询该店铺下特定状态的订单集合,采用分页查询
     *
     * @param orderRequest
     * @return
     */
    @Override
    public ReturnEntity findOrderList(OrderRequest orderRequest) {
        Integer storeId = getStoreId(orderRequest.getToken());
        if (storeId == null || storeId == 0) {
            return new ReturnEntity(501, Const.TOKEN + "过期");
        }
        orderRequest.setStoreId(storeId);
        if (Limit.semLimit.tryAcquire()) {
            List<Object> list = orderFactory.createQuery(orderRequest.getReminderStatus(),orderRequest.getRefundType())
                    .list(orderRequest);
            Limit.semLimit.release();
            return new ReturnEntity(list);
        } else {
            LogUtils.create().methodName("findOrderList").message("信号量控制").info();
            return new ReturnEntity(500, "信号量控制");
        }
    }

    /**
     * 根据订单号或者订单的取货号查询订单详情
     *
     * @param orderId
     * @param token
     * @return
     */
    @Override
    public ReturnEntity findOrderDetail(String orderId, String token) {
        Integer storeId = getStoreId(token);
        if (storeId == null || storeId == 0) {
            return new ReturnEntity(501, Const.TOKEN + "过期");
        }
        if (StringUtils.isEmpty(orderId)) {
            return new ReturnEntity();
        }
        /**
         * 判断orderId是否是订单号
         */
        List list = null;
        if (!ehCacheService.exists(orderId)) {
            String order = redisService.getMapString(Const.ORDER_PICK + storeId, orderId);
            if (StringUtils.isEmpty(order)) {
                List<String> orderIds = redisService.lpList(Const.PHONE + orderId);
                if (!orderIds.isEmpty()) {
                    list = ehCacheService.getList(orderIds);
                }
            } else {
                Object o = getOrder(order);
                list = Collections.singletonList(o);
            }
        } else {
            Object o = getOrder(orderId);
            list = Collections.singletonList(o);
        }
        if (null == list) {
            return orderClient.find(orderId, storeId);
        } else {
            return new ReturnEntity(list);
        }

    }

    /**
     * 接单接口,后期看情况简化删除,限流策略
     *
     * @param json
     * @return
     */
    @Override
    @ServiceLimit
    public ReturnEntity opt(JSONObject json) throws MallException {
        /**
         * 从缓存查询订单
         */
        String orderId = json.getString(Const.ORDER_ID);
        OrderResponse orderResponse = getOrder(orderId);
        if (null == orderResponse) {
            LogUtils.create().methodName("opt").addValue(Const.ORDER_ID,orderId).message(Const.ORDER_NOT_EXISTS).error();
            return new ReturnEntity(500, Const.ORDER_NOT_EXISTS);
        }
        Integer status = orderResponse.getTotalStatus();
        /**
         * 正常接单
         */
        orderFactory.createProcess(status).process(orderResponse);
        notifyService.sendNotify(orderResponse);
        return new ReturnEntity();
    }

    /**
     * 将订单置为已完成
     * @desc 该接口比较危险，谨慎调用
     * @param
     * @return
     */
    @Override
    public ReturnEntity orderFinish(JSONObject json) {
        String orderId = json.getString(Const.ORDER_ID);
        LogUtils.create().methodName("orderFinish").key(orderId).message("订单手动完成").info();
        OrderResponse orderResponse = getOrder(orderId);
        if (null == orderResponse) {
            return new ReturnEntity(500, "订单不存在");
        }
        ModifyOrderRequest modifyOrderRequest = new ModifyOrderRequest();
        modifyOrderRequest.setOrderId(orderId);
        modifyOrderRequest.setSendStatus(Const.SIX);
        modifyOrderRequest.setOrderStatus(Const.FIVE);
        modifyOrderRequest.setExceptionStatus(0);
        orderResponse.setTotalStatus(Const.SEVEN);
        orderResponse.setSendStatus(Const.SIX);
        orderResponse.setOrderStatus(Const.FIVE);
        orderResponse.setExceptionStatus(0);
        redisService.zrem(Const.REMINDER + getStoreId(json.getString(Const.TOKEN)), orderId);
        /**
         * 装载数据库
         */
        notifyService.sendNotify(orderResponse);
        Future<ReturnEntity> future = executorService.submit(new OrderModifyTask(modifyOrderRequest, orderClient));
        try {
            return future.get();
        } catch (Exception e) {
            LogUtils.create().methodName("orderFinish").key(orderId).message(e.getMessage()).error();
            return new ReturnEntity(500, "修改失败");
        }
    }

    /**
     * 查询店铺集合,主要作用于切换店铺
     * @param token
     * @return
     * @throws IOException
     */
    @Override
    public ReturnEntity findStoreId(String token) throws IOException {
        Map<String, String> map = redisService.getMap(Const.TOKEN_CHECK + token);
        List<Integer> keys = map.keySet().stream().map(d->Integer.parseInt(d)).collect(Collectors.toList());
        List<StoreResponse> stores = productService.findStoreList(keys);
        return new ReturnEntity(stores);
    }

    /**
     * 查询店铺商品集合
     * @param token
     * @return
     */
    @Override
    public ReturnEntity findProduct(String token) {
        Integer storeId = getStoreId(token);
        if(null == storeId){
            return new ReturnEntity(501,"token过期");
        }
        return productClient.findProduct(storeId);
    }

    /**
     * 修改店铺商品章台
     * @param json
     * @return
     */
    @Override
    public ReturnEntity updateProductStatus(JSONObject json) {
        return productClient.updateProductStatus(json);
    }

    /**
     * 查询条件历史订单
     * @param json
     * @return
     * @throws IOException
     */
    @Override
    public ReturnEntity findAllOrder(JSONObject json) throws IOException {
        if (Limit.hisLimit.tryAcquire()) {
            Date start = json.getTimestamp("start");
            Date end = json.getTimestamp("end");
            start = null == start ? DateUtil.getTime() : start;
            end = null == end ? new Date() : end;
            String deliver = "";
            Integer deliveryType = json.getInteger("deliveryType");
            if (deliveryType == 0) {
                deliver = "1,2";
            } else {
                deliver = deliveryType + "";
            }
            String token = json.getString(Const.TOKEN);
            Integer storeId = getStoreId(token);
            JSONObject object = json.getJSONObject(Const.ORDER_STATUS);
            Integer type = object.getInteger("type");
            String total = "";
            String refund = "";
            if (type == 0) {
                total = "1,2,3,4,5,6,7,8,9,97,98";
                refund = "0,1,2,18,19,28,29";
            } else {
                Object obj = object.get("totalStatus");
                Integer refundStatus = object.getInteger(Const.REFUND_STATUS);
                refund = refundStatus == 0 ? "0" : "1,2,18,19,28,29";
                if(null != obj){
                    List<Integer> totalStatus = (List<Integer>) obj;
                    for (Integer i : totalStatus) {
                        total = total + "," + i;
                    }
                    total = total.substring(1);
                }else{
                    total = "0";
                }
            }
            Integer pageNum = json.getInteger("pageNum");
            pageNum = null == pageNum || 0 == pageNum ? 1 : pageNum;
            Integer pageSize = json.getInteger("pageSize");
            pageSize = null == pageSize || 0 == pageSize ? 20 : pageSize;
            pageNum = (pageNum - 1) * pageSize;
            List<String> orderId = orderDao.history(new HistoryRequest(deliver, total, refund, start, end, storeId, pageNum, pageSize));
            String orderIds = "";
            List<OrderResponse> list = new ArrayList<>();
            for (String order : orderId) {
                OrderResponse orderResponse = getOrder(order);
                if (null == orderResponse) {
                    orderIds = order + ",";
                } else {
                    list.add(orderResponse);
                }
            }
            if (StringUtils.isEmpty(orderIds)) {
                Limit.hisLimit.release();
                return new ReturnEntity(list);
            }
            ReturnEntity entity = orderClient.detail(orderIds);
            if (null != entity && null != entity.getData()) {
                List<OrderResponse> response = MapperUtils.json2list(MapperUtils.obj2json(entity.getData()), OrderResponse.class);
                if (!response.isEmpty()) {
                    response.stream().forEach(d -> list.add(d));
                }
            }
            Limit.hisLimit.release();
            return new ReturnEntity(list);
        }
        return new ReturnEntity(500, "信号控制");
    }

    /**
     * 取消呼单,选择自配送
     *
     * @param orderId
     * @return
     */
    @Override
    public ReturnEntity cancelCallOrder(String orderId) {
        LogUtils.create().methodName("cancelCallOrder").addValue(Const.ORDER_ID,orderId).message("取消呼单,选择自配送").info();
        OrderResponse orderResponse = getOrder(orderId);
        if (null == orderResponse) {
            return new ReturnEntity(500, Const.ORDER_NOT_EXISTS);
        }
        if (Const.TWO != orderResponse.getTotalStatus()) {
            return new ReturnEntity(500, "该订单状态不允许取消");
        }
        orderFactory.createOrder().setCancelOrder(orderFactory.createCancel(2,0))
                .cancel(orderResponse);
        return new ReturnEntity();
    }

    @Override
    public ReturnEntity updateStoreStatus(JSONObject json) {
        Integer storeId = getStoreId(json.getString(Const.TOKEN));
        json.remove(Const.TOKEN);
        json.put(Const.STORE_ID, storeId);
        return productClient.updateStoreStatus(json);
    }

    @Override
    public ReturnEntity num(String token) {
        return orderClient.count(getStoreId(token));
    }

    /**
     * 切换店铺操作
     * @param token
     * @param storeId
     * @return
     * @throws IOException
     */
    @Override
    public ReturnEntity checkout(String token, Integer storeId) {
        Map<String, String> map = redisService.getMap(Const.TOKEN_CHECK + token);
        if(map.containsKey(storeId)){
            String value = map.get(String.valueOf(storeId));
            String store = redisService.getString(token);
            redisService.remove(map.get(store)+store);
            redisService.setString(token,String.valueOf(storeId),2592000);
            redisService.setString(value+storeId,token,2592000);
            return new ReturnEntity();
        }
        return new ReturnEntity(500,"权限不足");
    }

    private Integer getStoreId(String token) {
        String str = redisService.getString(token);
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        return Integer.valueOf(str);
    }

    private OrderResponse getOrder(String orderId) {
        /**
         * 本地缓存查询数据
         */
        OrderResponse orderResponse = (OrderResponse) ehCacheService.getObj(orderId);
        if (null == orderResponse) {
            /**
             * redis读取数据
             */
            orderResponse = redisService.getObject(orderId, OrderResponse.class);
        }
        /**
         * 不从order模块数据库拉取数据,防止退款操作的脏读
         */
        return orderResponse;
    }
}
