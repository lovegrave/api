package cn.yanss.m.cookhouse.api.dao.entity;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponse implements Serializable {

    private String productId;
    private String productName;
    private Integer price;
    private Integer status;
    private Integer count;
    private Integer weight;
    private JSONArray listCoupon;
    private String tags;
    private String image;
    private String intro;
    private Integer type;
    /**
     * [{
     * "count":0,
     * "refundStatus":18(退款成功),19(退款失败),9(拒绝退款)
     * }]
     */
    private JSONArray refundRecord;
}