package cn.yanss.m.cookhouse.api.feign.hystrix;

import cn.yanss.m.cookhouse.api.feign.ProductClient;
import cn.yanss.m.entity.ReturnEntity;
import cn.yanss.m.util.LogUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author
 */
@Component
public class ProductHystrix implements ProductClient {
    @Override
    public ReturnEntity findProduct(Integer productRequest) {
        LogUtils.create().methodName("ProductHystrix-->findProduct").message("查询商品接口调用失败").error();
        return new ReturnEntity(500, "调用失败");
    }

    @Override
    public ReturnEntity updateProductStatus(JSONObject productRequest) {
        LogUtils.create().methodName("updateProductStatus").message("修改商品状态失败").error();
        return new ReturnEntity(500, "调用失败");
    }

    @Override
    public ReturnEntity findStoreList(List<Integer> storeIds) {
        LogUtils.create().methodName("findStoreList").message("修改商品状态失败").error();
        return new ReturnEntity(500, "调用失败");
    }

    @Override
    public ReturnEntity findStore(Integer storeId) {
        LogUtils.create().methodName("findStore").key(String.valueOf(storeId)).message("查询店铺详情失败").error();
        return new ReturnEntity(500, "调用失败");
    }

    @Override
    public ReturnEntity updateStoreStatus(Map<String, Object> map) {
        LogUtils.create().methodName("updateStoreStatus").message("修改店铺上下架接口调用失败").error();
        return new ReturnEntity(500, "调用失败");
    }
}
