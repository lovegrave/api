package cn.yanss.m.cookhouse.api.dao.entity;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单所有参数
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse implements Serializable {
    /**
     * 重试机制，次数
     */
    private Integer times = 1;
    /**
     * 订单流程状态 1、已付款 2、已接单 3、配送中 4、已完成 5、配送回调异常/手动处理异常 6、厨房端操作异常 7、已退款 99、已取消
     */
    private Integer totalStatus;
    /**
     * 区别与totalStatus,它为totalStatus存入缓存之前的状态,存入缓存后,该状态改变
     */
    private Integer beforeStatus;
    /**
     * 订单的异常状态
     */
    private Integer exceptionStatus = 0;
    /**
     * 催单状态
     */
    private Integer reminderStatus = 0;
    /**
     * 退款状态
     */
    private Integer refundStatus = 0;

    private Date sendExceptionTime;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 订单流水号
     */
    private String orderNo;
    /**
     * 堂食取货码
     */
    private String orderPick;
    /**
     * 订单创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 订单总价格
     */
    private Integer payPrice;
    /**
     * 预计收货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date serviceTime;
    /**
     * 订单状态
     */
    private Integer orderStatus;
    /**
     * 配送状态
     */
    private Integer sendStatus;
    /**
     * 配送员姓名
     */
    private String riderName;
    /**
     * 配送员电话
     */
    private String riderPhone;

    /**
     * 骑手坐标
     */
    private String riderLocation;
    /**
     * 骑手截单时间
     */
    private Date riderPackTime;

    /**
     * 取货时间
     */
    private Date pickupTime;
    /**
     * 配送完成时间
     */
    private Date taskTime;
    /**
     * 完成时间
     */
    private Date finishTime;
    /**
     * 配送类型
     */
    private Integer sendType;
    /**
     * 配送公司
     */
    private Long sendId;

    /**
     * 美团配送id
     */
    private String mtPeisongId;
    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**
     * 购物车详情json
     */
    private Cart cartInfo;

    /**
     * 收货详情信息
     */
    private JSONObject consigneeInfo;

    /**
     * 用餐人数
     */
    private Integer diningCount;
    /**
     * 店铺id
     */
    private Integer storeId;
    /**
     * 美团公司
     */
    private String deliveryId;
    /**
     * 异常备注
     */
    private String exceptionRemark;
    /**
     * 预定类型1、预定 2、非预定
     */
    private Integer bookingType;
    /**
     * 订单备注
     */
    private String description;

    /**
     * 1.配送 2.堂食
     */
    private Integer deliveryType;
    /**
     * 取消代码
     */
    private Integer cancelCode;
    /**
     * 退款相关
     */
    private RefundResponse refundResponse;


}
