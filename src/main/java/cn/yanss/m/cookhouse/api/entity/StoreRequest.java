package cn.yanss.m.cookhouse.api.entity;

import lombok.Data;

@Data
public class StoreRequest {

    private Integer storeId;
    private Integer status;
    private String pid;
}
