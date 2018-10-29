package cn.yanss.m.cookhouse.api.service;


import cn.yanss.m.cookhouse.api.dao.entity.StoreResponse;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    List<StoreResponse> findStoreList(List<Integer> keys) throws IOException;
}
