package cn.yanss.m.cookhouse.api.feign.hystrix;

import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.feign.DispatcherClient;
import cn.yanss.m.entity.ReturnEntity;
import cn.yanss.m.util.LogUtils;
import org.springframework.stereotype.Component;

@Component
public class DispatcherHystrix implements DispatcherClient {
    @Override
    public ReturnEntity cancelCode(OrderResponse orderResponse) {
        LogUtils.create().methodName("cancelCode").key(orderResponse.getOrderId()).message("调度模块--->cancelCode接口调用错误").error();
        return new ReturnEntity(500, "接口错误");
    }

    @Override
    public ReturnEntity createOrder(OrderResponse jsonObject) {
        LogUtils.create().methodName("createOrder").key(jsonObject.getOrderId()).message("调度模块--->createOrder接口调用错误").error();
        return new ReturnEntity(500, "调用接口失败");
    }

    @Override
    public ReturnEntity findRiderLocation(OrderResponse orderResponse) {
        LogUtils.create().methodName("findRiderLocation").key(orderResponse.getOrderId()).message("调度模块--->findRiderLocation接口调用错误").error();
        return new ReturnEntity(500, "调用接口失败");
    }

    @Override
    public ReturnEntity queryOrder(OrderResponse orderResponse) {
        LogUtils.create().methodName("queryOrder").key(orderResponse.getOrderId()).message("接口调用错误").error();
        return new ReturnEntity(500, "调用接口失败");
    }
}
