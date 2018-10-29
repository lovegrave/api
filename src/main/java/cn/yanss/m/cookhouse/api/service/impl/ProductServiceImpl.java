package cn.yanss.m.cookhouse.api.service.impl;

import cn.yanss.m.cookhouse.api.dao.entity.StoreResponse;
import cn.yanss.m.cookhouse.api.feign.ProductClient;
import cn.yanss.m.cookhouse.api.service.ProductService;
import cn.yanss.m.cookhouse.api.utils.MapperUtils;
import cn.yanss.m.entity.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductClient productClient;

    @Autowired
    public ProductServiceImpl(ProductClient productClient) {
        this.productClient = productClient;
    }

    @Override
    public List<StoreResponse> findStoreList(List<Integer> keys) throws IOException {
        ReturnEntity entity = productClient.findStoreList(keys);
        return MapperUtils.json2list(MapperUtils.obj2json(entity.getData()),StoreResponse.class);
    }
}
