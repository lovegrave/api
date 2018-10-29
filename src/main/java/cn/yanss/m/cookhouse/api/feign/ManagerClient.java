package cn.yanss.m.cookhouse.api.feign;

import cn.yanss.m.cookhouse.api.feign.config.FeignConfig;
import cn.yanss.m.cookhouse.api.feign.hystrix.ManagerHystrix;
import cn.yanss.m.entity.ReturnEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "yanss-manager", url = "192.168.11.168:8081", configuration = FeignConfig.class, fallback = ManagerHystrix.class)
public interface ManagerClient {


    @PostMapping("/refund")
    ReturnEntity refund(@RequestBody Map<String, Object> map);

    @PostMapping("/api/v2/store/user/kitchen/login")
    ReturnEntity login(@RequestBody Map map);
}
