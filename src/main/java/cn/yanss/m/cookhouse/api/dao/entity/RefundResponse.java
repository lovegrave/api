package cn.yanss.m.cookhouse.api.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundResponse implements Serializable {
    /**
     * 退款中金额
     */
    private Integer applyRefundPrice = 0;
    /**
     * 以退款金额
     */
    private Integer refundedPrice = 0;
    /**
     * 退款失败金额
     */
    private Integer refundFailurePrice = 0;
    /**
     * 申请退款理由
     */
    private String refundReason;
    /**
     * 退款图片
     */
    private String refundPic;
    /**
     * 退款菜品
     */
    private String refundProduct;

}
