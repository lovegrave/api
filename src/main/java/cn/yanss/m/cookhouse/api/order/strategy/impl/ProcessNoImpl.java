package cn.yanss.m.cookhouse.api.order.strategy.impl;

import cn.yanss.m.cookhouse.api.constant.Const;
import cn.yanss.m.cookhouse.api.dao.entity.OrderResponse;
import cn.yanss.m.cookhouse.api.order.strategy.ProcessOrder;
import cn.yanss.m.exception.MallException;
import cn.yanss.m.util.LogUtils;
import org.springframework.stereotype.Component;

import static cn.yanss.m.exception.constant.ExceptionEnum.OPERATION_DATA_ERROR;

/**
 * 流程操作空对象模式,防止空指针
 * @author HL
 */
@Component
public class ProcessNoImpl implements ProcessOrder {

    @Override
    public void process(OrderResponse orderResponse) throws MallException {
        LogUtils.create().methodName("processNoImpl").addValue(Const.ORDER_ID,orderResponse.getOrderId()).message("该状态不能接单").error();
        throw new MallException(OPERATION_DATA_ERROR);
    }
}
