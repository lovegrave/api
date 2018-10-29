package cn.yanss.m.cookhouse.api.feign;


import cn.yanss.m.cookhouse.api.feign.config.FeignConfig;
import cn.yanss.m.cookhouse.api.feign.hystrix.ProductHystrix;
import cn.yanss.m.entity.ReturnEntity;
import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@FeignClient(name = "yanss-product", url = "http://192.168.11.176:8080", configuration = FeignConfig.class, fallback = ProductHystrix.class)
public interface ProductClient {

    @GetMapping("/api/v2/product/store/product")
    ReturnEntity findProduct(@RequestParam("storeId") Integer storeId);

    @PostMapping("/api/v2/product/update/product")
    ReturnEntity updateProductStatus(@RequestBody JSONObject json);

    @GetMapping("/api/v2/product/stores")
    ReturnEntity findStoreList(@RequestBody List<Integer> storeIds);

    @PostMapping("api/v2/product/store")
    ReturnEntity findStore(@RequestParam("storeId") Integer storeId);

    @PostMapping("/api/v2/product/update/store")
    ReturnEntity updateStoreStatus(@RequestBody Map<String, Object> map);
}
