package cn.yanss.m.cookhouse.api.entity;

import lombok.Data;

@Data
public class OrderRequest {

    /**
     * 状态
     */
    private Integer totalStatus;
    /**
     * 退款状态
     */
    private Integer refundType;
    /**
     * 催单状态
     */
    private Integer reminderStatus = 0;
    /**
     * 店铺id
     */
    private Integer storeId;
    /**
     * 请求token
     */
    private String token;
    /**
     * 起始页
     */
    private Integer currentPage = 1;
    /**
     * 每页的条数
     */
    private Integer pageSize = 20;

}
