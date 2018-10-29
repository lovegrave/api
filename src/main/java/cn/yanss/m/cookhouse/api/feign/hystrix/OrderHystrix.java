package cn.yanss.m.cookhouse.api.feign.hystrix;

import cn.yanss.m.cookhouse.api.entity.ModifyOrderRequest;
import cn.yanss.m.cookhouse.api.feign.OrderClient;
import cn.yanss.m.entity.ReturnEntity;
import cn.yanss.m.util.LogUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

/**
 * @author
 */
@Component
public class OrderHystrix implements OrderClient {
    @Override
    public ReturnEntity detail(String orderId) {
        LogUtils.create().methodName("OrderHystrix").key(orderId).message("订单模块--->detail接口调用失败").error();
        return new ReturnEntity(500, "接口调用失败");
    }

    @Override
    public ReturnEntity modifyOrderStatus(ModifyOrderRequest modifyOrderRequest) {
        LogUtils.create().methodName("modifyOrderStatus").key(modifyOrderRequest.getOrderId()).message("订单模块--->修改订单模块调用失败").error();
        return new ReturnEntity(500,"接口调用失败");
    }

    @Override
    public ReturnEntity findHistoryOrderList(JSONObject json) {
        LogUtils.create().methodName("findHistoryOrderList").message("订单模块--->查询订单接口调用失败").error();
        return new ReturnEntity(500,"接口调用失败");
    }

    @Override
    public ReturnEntity find(String condition, Integer storeId) {
        LogUtils.create().methodName("find").type("order").message("订单find接口调用法失败").error();
        return new ReturnEntity(500,"接口调用失败");
    }

    @Override
    public ReturnEntity count(Integer storeId) {
        LogUtils.create().methodName("count").type("order").message("订单统计count接口调用失败").error();
        return new ReturnEntity(500,"接口调用失败");
    }
}
