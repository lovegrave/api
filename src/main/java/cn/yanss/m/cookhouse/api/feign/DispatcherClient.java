package cn.yanss.m.cookhouse.api.feign;

import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.feign.hystrix.DispatcherHystrix;
import cn.yanss.m.entity.ReturnEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "yanss-dispatcher", url = "192.168.11.53:8081", fallback = DispatcherHystrix.class)
public interface DispatcherClient {

    @PostMapping("/api/v2/client/kitchen/express/scheduler/cancelOrder")
    ReturnEntity cancelCode(@RequestBody OrderResponse orderResponse);

    @PostMapping("/api/v2/client/kitchen/express/scheduler/createOrder")
    ReturnEntity createOrder(@RequestBody OrderResponse jsonObject);

    @PostMapping("/api/v2/client/kitchen/express/scheduler/findRiderLocation")
    ReturnEntity findRiderLocation(@RequestBody OrderResponse orderResponse);

    @PostMapping("/api/v2/client/kitchen/express/scheduler/queryOrder")
    ReturnEntity queryOrder(@RequestBody OrderResponse orderResponse);
}
