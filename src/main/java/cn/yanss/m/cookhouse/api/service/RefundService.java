package cn.yanss.m.cookhouse.api.service;

import cn.yanss.m.entity.ReturnEntity;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * @author hl
 * @desc 退款业务
 */
public interface RefundService {


    ReturnEntity refuseToRefund(Map<String, String> map) throws IOException;

    ReturnEntity orderRefund(JSONObject json) throws IOException;
}
