package cn.yanss.m.cookhouse.api.controller;

import cn.yanss.m.cookhouse.api.service.OrderService;
import cn.yanss.m.entity.ReturnEntity;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ReturnEntity create(@RequestBody JSONArray jsonArray){
        return orderService.create(jsonArray);
    }
}
