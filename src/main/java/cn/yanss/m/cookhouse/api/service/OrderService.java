package cn.yanss.m.cookhouse.api.service;

import cn.yanss.m.entity.ReturnEntity;
import com.alibaba.fastjson.JSONArray;

public interface OrderService {

    ReturnEntity create(JSONArray jsonArray);
}
