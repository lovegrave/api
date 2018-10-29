package cn.yanss.m.cookhouse.api.service.impl;

import cn.yanss.m.cookhouse.api.service.DispatcherService;
import cn.yanss.m.cookhouse.api.service.ManagerService;
import cn.yanss.m.cookhouse.api.service.RefundService;
import cn.yanss.m.entity.ReturnEntity;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ManagerServiceImpl implements ManagerService {
    @Autowired
    private DispatcherService dispatcherService;
    @Autowired
    private RefundService refundService;

    @Override
    public ReturnEntity riderLocation(String orderId) {
        return dispatcherService.findRiderLocation(orderId);
    }

    @Override
    public ReturnEntity refundOrder(JSONObject json) throws IOException {
        return refundService.orderRefund(json);
    }
}
