package cn.yanss.m.cookhouse.api.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cart implements Serializable {
    private Integer deliveryPrice;
    private Integer totalCouponPrice;
    private Integer totalBoxPrice;
    private List<ProductResponse> listProduct;
}
