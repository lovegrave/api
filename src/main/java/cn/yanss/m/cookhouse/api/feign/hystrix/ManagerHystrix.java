package cn.yanss.m.cookhouse.api.feign.hystrix;

import cn.yanss.m.cookhouse.api.feign.ManagerClient;
import cn.yanss.m.entity.ReturnEntity;
import cn.yanss.m.util.LogUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ManagerHystrix implements ManagerClient {

    private final ReturnEntity returnEntity = new ReturnEntity(500, "调用失败");

    @Override
    public ReturnEntity refund(Map<String, Object> map) {
        LogUtils.create().methodName("refund").key((String) map.get("orderId")).message("退款接口调用失败").error();
        return returnEntity;
    }

    @Override
    public ReturnEntity login(Map map) {
        LogUtils.create().methodName("login").message("登陆调用失败").error();
        return returnEntity;
    }


}
