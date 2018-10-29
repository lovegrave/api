package cn.yanss.m.cookhouse.api.dao.impl;

import cn.yanss.m.cookhouse.api.dao.RefundDao;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class RedundDaoImpl implements RefundDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void insertApply(OrderResponse orderResponse, Integer refundPrice, String productId) {
        String sql = "insert into tb_kitchen_refund(order_id, create_time, refund_status, refund_price, refund_reason, refund_pic, refund_product, store_id,refund_product_id) values (?,?,?,?,?,?,?,?,?)";
        Object[] objects = new Object[]{orderResponse.getOrderId(), new Date(), orderResponse.getRefundStatus(), refundPrice, orderResponse.getRefundResponse().getRefundReason(), orderResponse.getRefundResponse().getRefundPic(), orderResponse.getRefundResponse().getRefundProduct(), orderResponse.getStoreId(), productId};
        jdbcTemplate.update(sql, objects);
    }


}
