package cn.yanss.m.cookhouse.api.service;

import cn.yanss.m.entity.ReturnEntity;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

public interface ManagerService {

    ReturnEntity riderLocation(String orderId);

    ReturnEntity refundOrder(JSONObject map) throws IOException;
}
