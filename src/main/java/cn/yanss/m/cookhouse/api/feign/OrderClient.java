package cn.yanss.m.cookhouse.api.feign;

import cn.yanss.m.cookhouse.api.entity.ModifyOrderRequest;
import cn.yanss.m.cookhouse.api.feign.config.FeignConfig;
import cn.yanss.m.cookhouse.api.feign.hystrix.OrderHystrix;
import cn.yanss.m.entity.ReturnEntity;
import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "yanss-order", url = "192.168.11.214:8086", configuration = FeignConfig.class, fallback = OrderHystrix.class)
public interface OrderClient {

    @GetMapping(value = "/api/v2/order/id")
    ReturnEntity detail(@RequestParam("orderIds") String orderId);

    @PostMapping("/api/v2/order/update")
    ReturnEntity modifyOrderStatus(@RequestBody ModifyOrderRequest modifyOrderRequest);

    @PostMapping("/api/v2/order/list")
    ReturnEntity findHistoryOrderList(@RequestBody JSONObject json);

    @GetMapping(value = "api/v2/order/find/{condition}/{storeId}")
    ReturnEntity find(@PathVariable("condition") String condition, @PathVariable("storeId") Integer storeId);

    @GetMapping(value = "/api/v2/order/count/{storeId}")
    ReturnEntity count(@PathVariable("storeId") Integer storeId);

}
