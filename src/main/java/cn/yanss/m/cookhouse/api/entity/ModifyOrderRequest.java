package cn.yanss.m.cookhouse.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单修改数据封装类
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModifyOrderRequest implements Serializable {

    private String orderId;
    private Integer orderStatus;
    private Integer sendStatus;
    private Integer exceptionStatus;
    private String exceptionRemark;
    private String mtPeisongId;
    private String deliveryId;
    private Long sendId;
    private String riderName;
    private String riderPhone;
    private Integer sendType;
    private Integer refundStatus;
    private Integer commentStatus;
}
