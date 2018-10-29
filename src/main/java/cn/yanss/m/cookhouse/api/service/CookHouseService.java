package cn.yanss.m.cookhouse.api.service;

import cn.yanss.m.cookhouse.api.entity.OrderRequest;
import cn.yanss.m.entity.ReturnEntity;
import cn.yanss.m.exception.MallException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;


public interface CookHouseService {

    /**
     * @param orderRequest
     * @return
     * @throws InterruptedException
     */
    ReturnEntity findOrderList(OrderRequest orderRequest) throws InterruptedException;

    ReturnEntity findOrderDetail(String orderId, String token);

    ReturnEntity opt(JSONObject json) throws MallException;

    ReturnEntity orderFinish(JSONObject json);

    ReturnEntity findStoreId(String token) throws IOException;

    ReturnEntity findProduct(String token);

    ReturnEntity updateProductStatus(JSONObject jsonObject);

    ReturnEntity findAllOrder(JSONObject json) throws IOException;

    ReturnEntity cancelCallOrder(String orderId);

    ReturnEntity updateStoreStatus(JSONObject json);

    ReturnEntity num(String token);

    ReturnEntity checkout(String token, Integer storeId);
}
