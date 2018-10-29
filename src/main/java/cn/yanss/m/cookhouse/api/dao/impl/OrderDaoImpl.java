package cn.yanss.m.cookhouse.api.dao.impl;


import cn.yanss.m.cookhouse.api.dao.OrderDao;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.entity.HistoryRequest;
import cn.yanss.m.cookhouse.api.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单存储
 */
@Repository
public class OrderDaoImpl implements OrderDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void addOrder(OrderResponse orderResponse) {
        StringBuilder stringBuilder = new StringBuilder();
        Object[] obj = null;
        if(orderResponse.getTotalStatus() == 1){
            stringBuilder.append("insert into tb_kitchen_order(order_id,store_id,total_status,refund_status,create_date,order_status,delivery_type) values(?,?,?,?,?,?,?)");
            obj = new Object[]{orderResponse.getOrderId(), orderResponse.getStoreId(), orderResponse.getTotalStatus(),0, new Date(), orderResponse.getOrderStatus(), orderResponse.getDeliveryType()};
        }else{
            stringBuilder.append("update tb_kitchen_order set total_status = ?,refund_status = ?,update_time = ?,rider_name = ? ,rider_phone = ? ,send_type = ? , send_status = ?,send_company = ? where order_id = ?");
            obj = new Object[]{orderResponse.getTotalStatus(),orderResponse.getRefundStatus(),new Date(),orderResponse.getRiderName(),orderResponse.getRiderPhone(),orderResponse.getSendType(),orderResponse.getSendStatus(),orderResponse.getDeliveryId(),orderResponse.getOrderId()};
        }
        jdbcTemplate.update(stringBuilder.toString(), obj);
    }

    @Override
    public List<Map<String, Object>> queryList(String orderId) {
        String sql = "select * from tb_kitchen_order where order_id = " + orderId;
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> list(Date startTime, Date endTime) {
        String sql = "select * from tb_kitchen_order where create_date between " + startTime + " and " + endTime + " group by order_id";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<String> getOrderId() {
        String sql = "select order_id from tb_kitchen_order where create_date between ? and ? group by order_id";
        Object[] obj = new Object[]{DateUtil.getTime(), new Date()};
        return jdbcTemplate.queryForList(sql, obj, String.class);
    }

    @Override
    public List<String> history(HistoryRequest history) {
        String sql = "select order_id from tb_kitchen_order where create_date between ? and ? and delivery_type in ("+history.getDeliver()+") and store_id = ? and (total_status in("+history.getTotal()+") or refund_status in ("+history.getRefund()+")) limit ?,?";
        Object[] obj = new Object[]{history.getStart(), history.getEnd(), history.getStoreId(), history.getPageNum(), history.getPageSize()};
        return jdbcTemplate.queryForList(sql, obj, String.class);
    }
}
